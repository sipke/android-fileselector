/*
 * Copyright (c) 2017. Sipke Vriend
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of canstr nor the names of its contributors may be used
 *     to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package canstr.android.ui.fileselector;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class FileSelection {

    /**
     * An array of sample (dummy) items.
     */
    public static List<FileSelectionContent> ITEMS = new ArrayList<FileSelectionContent>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, FileSelectionContent> ITEM_MAP = new HashMap<String, FileSelectionContent>();

    public static List<File> updateFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        ITEMS.clear();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
//                inFiles.addAll(getListFiles(file));
                } else {
                    int index = ITEMS.size();
                    String name = file.getName();
                    addItem(createDummyItem(index, name, file));
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    private static void addItem(FileSelectionContent item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static FileSelectionContent createDummyItem(int position, String content, File file) {
        return new FileSelectionContent(String.valueOf(position), content, makeDetails(position), file);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class FileSelectionContent {
        public final String id;
        public String content;
        public final String details;
        public File file;
        public final long size;

        public FileSelectionContent(String id, String content, String details, File file) {
            this.id = id;
            this.content = content;
            this.details = details;
            this.file = file;
            this.size = file.length();
        }

        public void rename(String newName) {
            this.file = updateFileName(this.file, newName);
            this.content = this.file.getName();
        }

        @Override
        public String toString() {
            return content;
        }

        private File updateFileName(File from, String toName) {
            File parent = from.getParentFile();
            File to = new File(parent,toName);
            if (!from.renameTo(to)) {
                to = from;
            }
            return to;
        }

    }

}
