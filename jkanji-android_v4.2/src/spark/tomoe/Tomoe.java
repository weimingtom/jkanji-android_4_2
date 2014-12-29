package spark.tomoe;

import java.util.ArrayList;
import java.util.Collections;

import android.graphics.PointF;

//E:\libspark\as2\Tomoe\spark\tomoe

/**
 * 文字認識エンジン「巴」をベースとした、手書き文字認識クラス。
 * 
 * 巴：http://tomoe.sourceforge.jp/
 * 
 * @author  Y.Shindo
 * @see     NumberDictionary
 * @see     HiraganaDictionary
 * @version $Rev: 2 $
 */
public class Tomoe {
	private boolean ignore_stroke_len = true;
	
	private ArrayList<DictionaryItem> dictionary;

	/**
	 * 新しい文字認識クラスのインスタンスを生成します。
	 */
	public Tomoe(boolean ignore_stroke_len) {
		dictionary = new ArrayList<DictionaryItem>();
		this.ignore_stroke_len = ignore_stroke_len;
	}
	
	/**
	 * 新しい辞書を追加します。
	 * 
	 * 辞書は [文字名, 1画目, 2画目, ..., n画目] という配列を要素とした配列です。
	 * 一画は [始点ポイント, 中継ポイント1, 中継ポイント2, ..., 中継ポイントn, 終点ポイント] という配列です。
	 * ポイントは [x座標, y座標] という配列です。
	 * 
	 * @param   dictionary 辞書配列
	 * @see   NumberDictionary
	 * @see   HiraganaDictionary
	 */
	public void addDictionary(DictionaryItem[] dictionary) {
		if (dictionary != null) {
			for(int i = 0; i < dictionary.length; i++) {
				this.dictionary.add(dictionary[i]);
			}
		}
	}
	
	/**
	 * 手書き文字を判定し、最も近いと思われる文字を辞書から探します。
	 * 
	 * 結果は最大maxOut個を配列で返します。但し、必ずしも要素数はmaxOut個にはなりません。
	 * 全く候補が無い場合は、nullを返します。
	 * 
	 * 返される配列の要素は {letter: 文字, score: スコア} となります。スコアは小さければ小さいほど、
	 * 手書き文字に近いと判定されています。
	 * 
	 * 手書き文字の入力は [１画目, ２画目, ..., n画目] という配列で行います。
	 * 一画は [始点ポイント, 中継ポイント1, 中継ポイント2, ..., 中継ポイントn, 終点ポイント] という配列です。
	 * ポイントは [x座標, y座標] という配列です。
	 * 
	 * 各ポイントを単純化したりする必要はありません。但し、デフォルト辞書は 300x300 の正方形内に
	 * 書かれた文字を基準とした座標で登録されているので、それ以外の大きさの場合は、スケーリングを
	 * するようにして下さい。
	 * 例えば、100x100の正方形内に入力をさせたのなら、ポイントの座標を3倍する必要があります。
	 * 
	 * また、座標は入力領域の左上を(0, 0)とした座標系に予め変換しておくようにしてください。
	 * 入力領域が(100, 100)の位置にあるのならば、各ポイントの座標を100ずつ引く必要があります。
	 * 
	 * @param   inputStrokes 手書き文字の配列
	 * @param   maxOut       最大いくつの候補を返すか
	 * @return   最も手書き文字に近いと思われる文字とそのスコア。最大maxOut個。
	 */
	public ArrayList<ResultCandidate> getMatched(ArrayList<ArrayList<PointF>> inputStrokes, double maxOut) {
		// 入力文字の補正（単純化）
		inputStrokes = fixInputStrokes(inputStrokes);
		int inputStrokesLen = inputStrokes.size();
		if (inputStrokesLen <= 0) {
			// 判定する画が無いやん
			return null;
		}
		// 辞書リファレンス
		ArrayList<DictionaryItem> dic = dictionary;
		int dicLen = dic.size();
		// 候補
		// 辞書から候補を選び出し、どんどんテストにかけていく事で絞り込む
		ArrayList<Candidate> candidates = new ArrayList<Candidate>();
		// とりあえず、画数を見て同じものだけ候補に追加
		for (int i = 0; i < dicLen; ++i) {
			//FIXME:
			// 先頭に文字名があるのでlength-1
			if (dic.get(i).getStrokesLen() == inputStrokesLen) {
				candidates.add(new Candidate(dic.get(i), 0));
			}
		}
		// 1画ずつチェックして候補を絞り込む
		for (int i = 0; i < inputStrokesLen; ++i) {
			if (candidates.size() == 0) {
				// 候補がなくなってしまった．．．
				return null;
			}
			// 絞り込み
			candidates = narrowCandidates(inputStrokes.get(i), candidates);
		}
		// 残った候補をスコアでソート
		//candidates.sortOn("score", Array.NUMERIC);
		Collections.sort(candidates);
		// 結果を抽出
		ArrayList<ResultCandidate> resultCandidates = new ArrayList<ResultCandidate>();
		for(int i = 0; i < maxOut; ++i) {
			// もう候補がない
			//FIXME:
			if (i >= candidates.size() || candidates.get(i) == null) 
				break;
			resultCandidates.add(new ResultCandidate(candidates.get(i).c.c, candidates.get(i).score));
		}
		return resultCandidates;
	}
	
	/*
	 * 入力文字を単純化して、辞書との比較処理をしやすくする
	 */
	private ArrayList<ArrayList<PointF>>  fixInputStrokes(ArrayList<ArrayList<PointF>> inputStrokes) {
		ArrayList<ArrayList<PointF>> resultInputStrokes = new ArrayList<ArrayList<PointF>>();	// 補正後の手書き文字
		int inputStrokesLen = inputStrokes.size();
		for (int i = 0; i < inputStrokesLen; ++i) {
			ArrayList<PointF> inputStroke = inputStrokes.get(i);	// (i+1)画目
			int inputStrokeLen = inputStroke.size() - 1;
			ArrayList<PointF> resultInputStroke = new ArrayList<PointF>();	// 補正後の手書き文字の1画
			if(inputStrokeLen > 0) {
				PointF p = inputStroke.get(0);	// 頂点0
				PointF q = inputStroke.get(1);	// 頂点1
				// とりあえず頂点1は積んじゃう
				PointF temp = new PointF();
				temp.set(p);
				resultInputStroke.add(temp);
				// ベクトル頂点0→1の角度
				double lastAngle = Math.atan2((q.y - p.y), (q.x - p.x));
				// 角度の差の累積
				double total = 0;
				for (int j = 1; ; ++j) {
					//System.out.println("=============>" + j + ", " + inputStrokeLen);
					//if (j < inputStrokeLen) {
					p = inputStroke.get(j);	// 頂点j
					if (j + 1 < inputStroke.size()) {
						q = inputStroke.get(j + 1);	// 頂点(j+1)
					} else {
						q = null;
					}
					//}
					if (j < inputStrokeLen) {
						// ベクトル頂点j→(j+1)の角度
						double angle = Math.atan2((q.y - p.y), (q.x - p.x));
						// 前回の角度との差を加算
						total += Math.abs(lastAngle - angle);
						// 保存
						lastAngle = angle;
						// 角度の差が一定値を越えたら新しい頂点を追加
						if (total >= 0.8) {
							// 但しあんまりにも線の長さが無い時はスルー（マウス等によるブレの補正）
							{
								int resultInputStrokeLen = resultInputStroke.size();
								PointF rp = resultInputStroke.get(resultInputStrokeLen - 1);
								double xd = rp.x - p.x;
								double yd = rp.y - p.y;
								// 最後に追加した補正後の点から今追加しようとしている点までの距離（の2乗）と比較
								if ((xd * xd + yd * yd) <= 30 * 30) {
									continue;
								}
							}
							total = 0;
							// 点を追加
							PointF temp2 = new PointF();
							temp2.set(p);
							resultInputStroke.add(temp2);
						}
					} else {
						// もう最後の点（j==inputStroke）なので問答無用で追加
						PointF temp3 = new PointF();
						temp3.set(p);
						resultInputStroke.add(temp3);
						break;
					}
				}		
				// 1画完成したので追加
				resultInputStrokes.add(resultInputStroke);
			}
		}
		return resultInputStrokes;
	}
	
	/*
	 * inputStrokeとcandidatesを比較して、候補を絞り込む
	 */
	private ArrayList<Candidate> narrowCandidates(ArrayList<PointF> inputStroke, ArrayList<Candidate> candidates) {
		// 残った候補を入れる配列
		ArrayList<Candidate> resultCandidates = new ArrayList<Candidate>();
		int inputStrokeLen = inputStroke.size();
		int candidatesLen = candidates.size();
		double inputStrokeFirstX = inputStroke.get(0).x;
		double inputStrokeFirstY = inputStroke.get(0).y;
		double inputStrokeLastX = inputStroke.get(inputStrokeLen - 1).x;
		double inputStrokeLastY = inputStroke.get(inputStrokeLen - 1).y;
		for (int i = 0; i < candidatesLen; ++i) {
			// 候補を取り出す
			Candidate candidate = candidates.get(i);
			DictionaryItem candidateStrokes = candidate.c;
			int candidateStrokesLen = candidateStrokes.getStrokesLen();	// -1は文字名の分
			for (int j = 0; j < candidateStrokesLen; ++j) {
				int[][] candidateStroke = candidateStrokes.d[j];	// 候補の(j+1)画目
				int candidateStrokeLen = candidateStroke.length;
				// あまりにも特徴点の数が違えばスルー
				if (!ignore_stroke_len && Math.abs(inputStrokeLen - candidateStrokeLen) > 3) {
					continue;
				}
				// 1画の始点同士の距離を算出
				int[] candidateStrokeFirst = candidateStroke[0];
				//FIXME:(int)
				int dx = (int)(candidateStrokeFirst[0] - inputStrokeFirstX);
				int dy = (int)(candidateStrokeFirst[1] - inputStrokeFirstY);
				int d1 = (dx * dx + dy * dy);
				if (d1 > 90 * 90)
					continue;
				// 1画の終点同士の距離を算出
				int[] candidateStrokeLast = candidateStroke[candidateStrokeLen - 1];
				//FIXME: (int)
				dx = (int)(candidateStrokeLast[0] - inputStrokeLastX);
				dy = (int)(candidateStrokeLast[1] - inputStrokeLastY);
				double d2 = (dx * dx + dy * dy);
				if (d2 > 90 * 90) 
					continue;
				// 入力画と候補の(j+1)画目を比較してスコア算出
				double score1 = calculateScore(inputStroke, transArray(candidateStroke));
				if (score1 < 0) 
					continue;
				// 候補の(j+1)画目と入力画を比較してスコア算出
				double score2 = calculateScore(transArray(candidateStroke), inputStroke);
				if (score2 < 0) 
					continue;
				// スコア加算
				candidate.score += (d1 + d2 + score1 + score2);
				// この候補は残しておこう
				resultCandidates.add(candidate);
				break;
			}
		}
		return resultCandidates;
	}
	
	//FIXME:
	private ArrayList<PointF> transArray(int[][] array) {
		ArrayList<PointF> result = new ArrayList<PointF>();
		for (int i = 0; i < array.length; i++) {
			PointF p = new PointF();
			p.x = array[i][0];
			p.y = array[i][1];
			result.add(p);
		}
		return result;
	}
	
	/**
	 * inputStrokeとmatchStrokeを比較してスコアを算出する。スコアが低いほど似ている。
	 * あまりにも違う場合、-1を返す。
	 */
	private double calculateScore(ArrayList<PointF> inputStroke, ArrayList<PointF> matchStroke) {
		double score = 0;
		int inputStrokeLen = inputStroke.size();
		int matchStrokeLen = matchStroke.size();
		// inputStrokeの各線分の角度を算出
		//FIXME:
		double[] inputStrokeAngle = new double[inputStrokeLen];
		for (int i = 0; i < inputStrokeAngle.length; i++) {
			inputStrokeAngle[i] = Double.NaN;
		}
		for (int i = 0; i < (inputStrokeLen - 1); ++i) {
			PointF p = inputStroke.get(i);
			PointF q = inputStroke.get(i + 1);
			inputStrokeAngle[i] = Math.atan2((q.y - p.y), (q.x - p.x));
		}
		// matchStrokeの各線分の角度を算出
		//FIXME:
		double[] matchStrokeAngle = new double[matchStrokeLen];
		for (int i = 0; i < matchStrokeAngle.length; i++) {
			matchStrokeAngle[i] = Double.NaN;
		}
		for (int i = 0; i < (matchStrokeLen - 1); ++i) {
			PointF p = matchStroke.get(i);
			PointF q = matchStroke.get(i + 1);
			matchStrokeAngle[i] = Math.atan2((q.y - p.y), (q.x - p.x));
		}
		// 最後に比較を中断した点から始められるように
		int matchStrokeOffset = 0; //FIXME: int ? double ?
		int j;
		// inputStroke主体で比較
		for (int i = 0; i < inputStrokeLen; ++i) {
			PointF p = inputStroke.get(i);
			// 最後に比較を中断した点から続ける
			for (/*double */j = matchStrokeOffset; j < matchStrokeLen; ++j) {
				PointF dp = matchStroke.get((int)j);
				// inputStroke[i]とmatchStroke[j]の距離を算出
				double dx = p.x - dp.x;
				double dy = p.y - dp.y;
				double d = dx * dx + dy * dy;
				if (j < (matchStrokeLen - 1)) {
					// 始点と始点の距離と線分の向き（角度）が一定以内
					if(d < 90 * 90 && Math.abs(inputStrokeAngle[i] - matchStrokeAngle[j]) < (Math.PI / 2)) {
						// この点の比較は終わり
						matchStrokeOffset = j;
						// スコア加算
						score += d;
						break;
					} else { 
						// inputStroke[i]とベクトルmatchStroke[j]→[j+1]との距離で比較する（点と線分の距離）
						// ベクトルmatchStroke[j]→matchStroke[j+1] : A
						double ax = matchStroke.get(j + 1).x - dp.x;
						double ay = matchStroke.get(j + 1).y - dp.y;
						// ベクトルinputStroke[i]→matchStroke[j] : B
						double bx = dx;
						double by = dy;
						// 媒介変数tを求め、inputStroke[i]からAへの垂線が存在するか調べる
						double t = (ax * bx + ay * by) / (ax * ax + ay * ay);
						if(t >= 0 && t <= 1) {
							// ベクトルmatchStroke[j]→交点
							ax *= t;
							ay *= t;
							// ベクトルinputStroke[i]→交点
							ax -= bx;
							ay -= by;
							// そのスカラー値が距離
							d = ax * ax + ay * ay;
							// 距離と角度が一定以内
							if (!Double.isNaN(inputStrokeAngle[i]) && !Double.isNaN(matchStrokeAngle[(int)j])) {
							if (d < 120 * 120 && 
								Math.abs(inputStrokeAngle[i] - matchStrokeAngle[j]) < (Math.PI / 2)) {
								// この点の比較は終わり
								matchStrokeOffset = j;
								// スコア加算
								score += d;
								break;
							}
							}
						}
					}
				} else {
					// 最後の点なので単純に距離比較だけ
					if (d < 90 * 90) {
						// この点の比較は終わり
						matchStrokeOffset = j;
						// スコア加算
						score += d;
						break;
					}
				}
			}
			// 近い距離の点が無く最後まで来てしまった．．．
			if (j >= matchStrokeLen) {
				return -1;
			}
		}
		return score;
	}
	
	/**
	 * letter に対応する辞書の項目に strokes を学習させる、もしくは追加します。
	 * 
	 * 辞書に既に同じ画数の letter が存在すれば、その字形を strokes で指定されたものに近い形に
	 * 修正をします。
	 * 
	 * それ以外の場合、辞書に新しく項目を追加します。
	 * 
	 * 配列の内容については他のメソッドを参照。
	 * 
	 * @param   letter  学習/追加する文字名
	 * @param   strokes 学習/追加する字形
	 * @return   更新（学習）した場合はtrue, 追加した場合はfalse
	 */
	public boolean study(char letter, ArrayList<ArrayList<PointF>> strokes) {
		// 入力文字の補正（単純化）
		strokes = fixInputStrokes(strokes);
		int strokesLen = strokes.size();
		ArrayList<DictionaryItem> dic = dictionary;
		int dicLen = dic.size();
		//FIXME:
		//int modifyIndex;
		int i;
		for(i = 0; i<dicLen; ++i) {
			DictionaryItem dicStrokes = dic.get(i);
			// 文字名と画数を比較
			if (dicStrokes.c == letter && dicStrokes.getStrokesLen() == strokesLen) {
				for(int j = 0; j < strokesLen; ++j) {
					ArrayList<PointF> stroke = strokes.get(j);
					int[][] dicStroke = dicStrokes.d[j];	// +1は文字名分					
					// 特徴点の数が同じ
					if(stroke.size() == dicStroke.length) {
						// そのまま平均を取る
						int dicStrokeLen = dicStroke.length;
						for(int n = 0; n < dicStrokeLen; ++n) {
							int[] p = dicStroke[n];
							PointF q = stroke.get(n);
							p[0] = (int)((p[0] + q.x) / 2);
							p[1] = (int)((p[1] + q.y) / 2);
						}
					} else { // 違う
						// 辞書の数に合わせて平均を取る
						int dicStrokeLen = dicStroke.length;
						double div = stroke.size() / dicStrokeLen;
						for (int n = 0; n < dicStrokeLen; ++n) {
							int[] p = dicStroke[n];
							PointF q;
							if (n < (dicStrokeLen - 1)) {
								q = stroke.get((int)Math.floor(div * n));
							} else {
								q = stroke.get(stroke.size() - 1);
							}
							p[0] = (int)((p[0] + q.x) / 2);
							p[1] = (int)((p[1] + q.y) / 2);
						}
					}
				}
				//FIXME:
				//modifyIndex = i;
				break;
			}
		}
		// 既存の辞書には見つからなかった
		if (i == dicLen) {
			// 辞書に新しく追加
			//FIXME:
			//DictionaryItem dicStrokes = strokes.slice();
			int[][][] d = new int[strokes.size()][][];
			for (int k = 0; k < d.length; k++) {
				d[k] = new int[strokes.get(k).size()][];
				for (int k2 = 0; k2 < d[k].length; k2++) {
					d[k][k2] = new int[2];
					d[k][k2][0] = (int)strokes.get(k).get(k2).x;
					d[k][k2][1] = (int)strokes.get(k).get(k2).y;
				}
			}
			DictionaryItem dicStrokes = new DictionaryItem(letter, d);
			//dicStrokes.unshift(letter);
			dic.add(dicStrokes);	
			return false;
		}	
		return true;
	}
}
