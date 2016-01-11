package view.component.dataview;

import javax.swing.*;

/**
 * Created by maeglin89273 on 9/3/15.
 */
public class MessageBar extends JPanel {
    private JProgressBar progressBar;
    private JLabel msgLbl;

    public MessageBar() {
        this.setupUIs();
    }

    private void setupUIs() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.msgLbl = new JLabel();
        this.progressBar = new JProgressBar();
        this.progressBar.setMinimum(0);

        this.add(this.msgLbl);
        this.add(this.progressBar);

        this.progressBar.setVisible(false);
    }

    public void setProgressIndeterminate() {
        this.hideMessageLbl();
        this.progressBar.setValue(0);
        this.progressBar.setIndeterminate(true);
    }

    public void setProgressMax(int max) {
        this.progressBar.setMaximum(max);
    }

    public void setProgress(int progress) {
        this.hideMessageLbl();
        if (this.progressBar.isIndeterminate()) {
            this.progressBar.setIndeterminate(false);
        }

        this.progressBar.setValue(progress);
    }

    public void setMessage(String text) {
        this.hideProgressBar();
        this.msgLbl.setText(text);
    }

    public void clear() {
        this.setMessage("");
    }

    private void hideMessageLbl() {
        if (this.msgLbl.isVisible()) {
            this.msgLbl.setVisible(false);
            this.progressBar.setVisible(true);
        }
    }

    private void hideProgressBar() {
        if (this.progressBar.isVisible()) {
            this.progressBar.setVisible(false);
            this.msgLbl.setVisible(true);
        }
    }
}
