/*
 * Decompiled with CFR 0.152.
 */
package AAVSOtools;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GBC
extends GridBagConstraints {
    public GBC(int n, int n2) {
        this.gridx = n;
        this.gridy = n2;
    }

    public GBC(int n, int n2, int n3, int n4) {
        this.gridx = n;
        this.gridy = n2;
        this.gridwidth = n3;
        this.gridheight = n4;
    }

    public GBC setSpan(int n, int n2) {
        this.gridwidth = n;
        this.gridheight = n2;
        return this;
    }

    public GBC setAnchor(int n) {
        this.anchor = n;
        return this;
    }

    public GBC setFill(int n) {
        this.fill = n;
        return this;
    }

    public GBC setWeight(double d, double d2) {
        this.weightx = d;
        this.weighty = d2;
        return this;
    }

    public GBC setInsets(int n) {
        this.insets = new Insets(n, n, n, n);
        return this;
    }

    public GBC setInsets(int n, int n2, int n3, int n4) {
        this.insets = new Insets(n, n2, n3, n4);
        return this;
    }

    public GBC setIpad(int n, int n2) {
        this.ipadx = n;
        this.ipady = n2;
        return this;
    }
}
