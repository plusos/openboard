// SPDX-License-Identifier: Apache-2.0
package org.dslul.openboard.inputmethod.latin.inputlogic;

import org.junit.Assert;
import org.junit.Test;

public class InputLogicInlineEmojiTest {

    @Test
    public void testIsStartOfInlineEmojiSearch() {
        // Valid start: after colon with preceding space/start
        Assert.assertTrue(InputLogic.isStartOfInlineEmojiSearch('s', ':', ' ', null));
        Assert.assertTrue(InputLogic.isStartOfInlineEmojiSearch('s', ':', -1, null));

        // Invalid start: colon after colon (::)
        Assert.assertFalse(InputLogic.isStartOfInlineEmojiSearch(':', ':', ' ', null));

        // Invalid start: colon followed by whitespace (: )
        Assert.assertFalse(InputLogic.isStartOfInlineEmojiSearch(' ', ':', ' ', null));

        // Invalid start: colon directly after a digit (12:30)
        Assert.assertFalse(InputLogic.isStartOfInlineEmojiSearch('3', ':', '2', null));
    }

    @Test
    public void testGetInlineEmojiSearchString() {
        // Basic query
        Assert.assertEquals("joy", InputLogic.getInlineEmojiSearchString(":joy"));
        Assert.assertEquals("joy", InputLogic.getInlineEmojiSearchString("hello :joy"));
        Assert.assertEquals("joy cat", InputLogic.getInlineEmojiSearchString(":joy cat"));

        // Preceded by surrogate pair emoji
        Assert.assertEquals("smile", InputLogic.getInlineEmojiSearchString("🎉:smile"));

        // Invalid queries
        Assert.assertNull(InputLogic.getInlineEmojiSearchString("::"));
        Assert.assertNull(InputLogic.getInlineEmojiSearchString(":"));
        Assert.assertNull(InputLogic.getInlineEmojiSearchString(": "));
        Assert.assertNull(InputLogic.getInlineEmojiSearchString("word:test")); // without space/word boundary
        Assert.assertNull(InputLogic.getInlineEmojiSearchString("12:30")); // digit before colon
        Assert.assertNull(InputLogic.getInlineEmojiSearchString(null));
        Assert.assertNull(InputLogic.getInlineEmojiSearchString(""));
    }
}
