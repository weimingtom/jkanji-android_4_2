/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calculator2mod;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class HistoryEntry {
    private String mBase;
    private String mEdited;

    public HistoryEntry(String str) {
        mBase = str;
        clearEdited();
    }

    public HistoryEntry(int version, DataInput in) throws IOException {
        mBase = in.readUTF();
        mEdited = in.readUTF();
    }
    
    public void write(DataOutput out) throws IOException {
        out.writeUTF(mBase);
        out.writeUTF(mEdited);
    }

    @Override
    public String toString() {
        return mBase;
    }

    public void clearEdited() {
        mEdited = mBase;
    }

    public String getEdited() {
        return mEdited;
    }

    public void setEdited(String edited) {
        mEdited = edited;
    }

    public String getBase() {
        return mBase;
    }
}
