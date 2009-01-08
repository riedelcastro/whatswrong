package com.googlecode.whatswrong;


import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


/**
 * A text field for search/filter interfaces. For now this ncludes a placeholder string (when the user hasn't yet typed
 * anything).
 *
 * @author Elliott Hughes, Sebastian Riedel
 */
public class FilterTextField extends JTextField {


    /**
     * Should notifications be send at each keystroke.
     */
    private boolean sendsNotificationForEachKeystroke = false;

    /**
     * Is the placeholder text currently shown.
     */
    private boolean showingPlaceholderText = false;


    /**
     * Creates a new FilterTextField with the given placehold text.
     *
     * @param placeholderText the placeholder text that appears if nothing has been entered into the field and the focus
     *                        is lost.
     */
    public FilterTextField(String placeholderText) {

        super();
        addFocusListener(new PlaceholderText(placeholderText));
        initKeyListener();

    }


    /**
     * A FilterTextField with default placeholder text ("Search").
     */
    public FilterTextField() {

        this("Search");

    }


    /**
     * This adds a key listener that reacts to the escape key entered into the text field. If pressed, the text will be
     * cleared.
     */
    private void initKeyListener() {

        addKeyListener(new KeyAdapter() {


            /**
             * @see java.awt.event.KeyAdapter#keyReleased(KeyEvent)
             */
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

                    cancel();

                } else if (sendsNotificationForEachKeystroke) {

                    maybeNotify();

                }

            }

        });

    }


    /**
     * Method cancel clears the text field.
     */
    private void cancel() {

        setText("");

        postActionEvent();

    }


    /**
     * Method maybeNotify performs post action events in case <code>showingPlaceholderText</code> is false.
     */
    private void maybeNotify() {

        if (showingPlaceholderText) {

            return;

        }

        postActionEvent();

    }


    /**
     * Sets whether notifications should be send after each keystroke on the text field.
     *
     * @param eachKeystroke true iff notifications should be send after each keystroke on the text field.
     */
    public void setSendsNotificationForEachKeystroke(boolean eachKeystroke) {

        this.sendsNotificationForEachKeystroke = eachKeystroke;

    }


    /**
     * Replaces the entered text with a gray placeholder string when the search field doesn't have the focus. The entered
     * text returns when we get the focus back.
     */
    class PlaceholderText implements FocusListener {

        /**
         * The actual placeholder text.
         */
        private String placeholderText;

        /**
         * The text previously in the text field.
         */
        private String previousText = "";

        /**
         * The color used previously.
         */
        private Color previousColor;


        /**
         * Creates a new placeholder text.
         *
         * @param placeholderText the placeholder text to display.
         */
        PlaceholderText(String placeholderText) {

            this.placeholderText = placeholderText;
            focusLost(null);

        }


        /**
         * If focus is gained the previous text is displayed.
         *
         * @param e the focus event.
         */
        public void focusGained(FocusEvent e) {

            setForeground(previousColor);
            setText(previousText);
            showingPlaceholderText = false;

        }


        /**
         * Method focusLost remembers the previous text and color and if the previous text is empty the placeholder text is
         * shown in gray color.
         *
         * @param e of type FocusEvent
         */
        public void focusLost(FocusEvent e) {

            previousText = getText();
            previousColor = getForeground();
            if (previousText.length() == 0) {
                showingPlaceholderText = true;
                setForeground(Color.GRAY);
                setText(placeholderText);
            }

        }

    }

}
