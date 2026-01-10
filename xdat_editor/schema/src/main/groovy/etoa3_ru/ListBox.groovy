package etoa3_ru

import acmi.l2.clientmod.util.IOUtil
import acmi.l2.clientmod.util.defaultio.DefaultIO
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@DefaultIO
@CompileDynamic
class ListBox extends DefaultProperty {
    int maxRow
    int showRow
    Boolean showLastLine

    // @formatter:off
    @Deprecated int getUnk100() { maxRow }
    @Deprecated void setUnk100(int unk100) { this.maxRow = unk100 }

    @Deprecated int getUnk101() { showRow }
    @Deprecated void setUnk101(int unk101) { this.showRow = unk101 }

    @Deprecated int getUnk102() { IOUtil.boolToInt(showLastLine) }
    @Deprecated void setUnk102(int unk102) { this.showLastLine = IOUtil.intToBool(unk102) }
    // @formatter:on
}
