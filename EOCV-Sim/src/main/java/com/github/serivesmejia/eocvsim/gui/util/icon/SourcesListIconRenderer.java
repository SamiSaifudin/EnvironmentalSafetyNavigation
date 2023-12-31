/*
 * Copyright (c) 2021 Sebastian Erives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.github.serivesmejia.eocvsim.gui.util.icon;

import com.github.serivesmejia.eocvsim.gui.EOCVSimIconLibrary;
import com.github.serivesmejia.eocvsim.gui.Icons;
import com.github.serivesmejia.eocvsim.input.InputSourceManager;

import javax.swing.*;
import java.awt.*;

public class SourcesListIconRenderer extends DefaultListCellRenderer {

    public static final int ICO_W = 15;
    public static final int ICO_H = 15;

    public InputSourceManager sourceManager = null;

    ImageIcon imageIcon = null;
    ImageIcon camIcon = null;
    ImageIcon vidIcon = null;

    public SourcesListIconRenderer(InputSourceManager sourceManager) {
        this.sourceManager = sourceManager;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        // Get the renderer component from parent class
        JLabel label = (JLabel) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);

        switch (sourceManager.getSourceType((String) value)) {
            case IMAGE:
                if(imageIcon == null) {
                    imageIcon = EOCVSimIconLibrary.INSTANCE.getIcoImg().resized(15, 15);
                }
                label.setIcon(imageIcon);
                break;
            case CAMERA:
                if(camIcon == null) {
                    camIcon = EOCVSimIconLibrary.INSTANCE.getIcoCam().resized(15, 15);
                }
                label.setIcon(camIcon);
                break;
            case VIDEO:
                if(vidIcon == null) {
                    vidIcon = EOCVSimIconLibrary.INSTANCE.getIcoVid().resized(15, 15);
                }
                label.setIcon(vidIcon);
                break;
        }

        return label;

    }

}
