package etoa3__

import acmi.l2.clientmod.util.Description
import acmi.l2.clientmod.util.defaultio.DefaultIO
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@DefaultIO
@CompileDynamic
class ChatWindow extends DefaultProperty {
    @Description('Vertical space between text lines')
    int lineGap
    @Description('-9999')
    int nextLineOffsetX = -9999

    // @formatter:off
    @Deprecated int getSpacing() { lineGap }
    @Deprecated void setSpacing(int spacing) { this.lineGap = spacing }

    @Deprecated int getUnk101() { nextLineOffsetX }
    @Deprecated void setUnk101(int unk101) { this.nextLineOffsetX = unk101 }
    // @formatter:on
}
