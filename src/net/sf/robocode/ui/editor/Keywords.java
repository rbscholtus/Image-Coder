/*******************************************************************************
 * Copyright (c) 2001, 2010 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Flemming N. Larsen
 *     - This class was made final
 *     - The constructor was removed
 *     - The string constant 'keywords' was renamed into 'KEYWORDS'
 *******************************************************************************/
package net.sf.robocode.ui.editor;

import java.util.Arrays;
import java.util.Comparator;
import javax.swing.text.Segment;

/**
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */
public final class Keywords {

    private final static String KEYWORDS[] = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false"
    };
    private final static Keywords instance = new Keywords();
    private static int shortest;
    private static int longest;

    private Keywords() {
        Arrays.sort(KEYWORDS, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                int res = o1.length() - o2.length();
                return res;
            }
        });
        shortest = KEYWORDS[0].length();
        longest = KEYWORDS[KEYWORDS.length-1].length();
    }

    public static boolean isKeyword(Segment seg) {
        if (seg.count < shortest || seg.count > longest) {
            return false;
        }

        int i = 0;
        while (KEYWORDS[i].length() < seg.count) {
            i++;
        }

        while (i < KEYWORDS.length && KEYWORDS[i].length() == seg.count) {
            int j = 0;
            for (; j < seg.count; j++) {
                if (seg.array[seg.offset + j] != KEYWORDS[i].charAt(j)) {
                    break;
                }
            }
            if (j == seg.count) {
                return true;
            }
            i++;
        }
        return false;
    }
}
