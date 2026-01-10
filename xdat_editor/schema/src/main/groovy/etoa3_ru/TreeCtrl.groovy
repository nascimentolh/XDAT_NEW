package etoa3_ru

import acmi.l2.clientmod.util.IOUtil
import acmi.l2.clientmod.util.defaultio.DefaultIO
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@DefaultIO
@CompileDynamic
class TreeCtrl extends DefaultProperty {
    Boolean saveExpandedNode = false
    Boolean multiExpand

    // @formatter:off
    @Deprecated boolean getUnk100() { saveExpandedNode }
    @Deprecated void setUnk100(boolean unk100) { this.saveExpandedNode = unk100 }

    @Deprecated int getUnk101() { IOUtil.boolToInt(multiExpand) }
    @Deprecated void setUnk101(int unk101) { this.multiExpand = IOUtil.intToBool(unk101) }
    // @formatter:on
}
