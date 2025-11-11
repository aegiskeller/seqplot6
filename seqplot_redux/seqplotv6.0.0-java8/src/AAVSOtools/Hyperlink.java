/*
 * Decompiled with CFR 0.152.
 */
package AAVSOtools;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Hyperlink
extends JFrame {
    public Hyperlink(String string) throws Exception {
        JEditorPane jEditorPane = new JEditorPane(string);
        jEditorPane.setEditable(false);
        final JEditorPane jEditorPane2 = jEditorPane;
        jEditorPane.addHyperlinkListener(new HyperlinkListener(){

            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                try {
                    if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        jEditorPane2.setPage(hyperlinkEvent.getURL());
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        this.setContentPane(new JScrollPane(jEditorPane));
        this.setSize(400, 400);
        this.setVisible(true);
        this.setDefaultCloseOperation(2);
    }
}
