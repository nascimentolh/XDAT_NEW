package etoa5

import acmi.l2.clientmod.util.defaultio.DefaultIO
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@DefaultIO
@CompileDynamic
class DrawPanel extends DefaultProperty {
    int autoSize

    // @formatter:off
    @Deprecated int getUnk100() { autoSize }
    @Deprecated void setUnk100(int unk100) { this.autoSize = unk100 }
    // @formatter:on
}
