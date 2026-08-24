// SPDX-License-Identifier: Apache-2.0
package org.dslul.openboard.inputmethod.latin.inputlogic;

import org.junit.Assert;
import org.junit.Test;

public class InputLogicInlineEmojiTest {

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
