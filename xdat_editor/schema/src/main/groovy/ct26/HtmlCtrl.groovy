package ct26

import acmi.l2.clientmod.util.Description
import acmi.l2.clientmod.util.defaultio.DefaultIO
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@DefaultIO
@CompileDynamic
class HtmlCtrl extends DefaultProperty {
    @Description("''/'Normal'/'Help'/'BBS'")
    String viewType

    // @formatter:off
    @Deprecated String getType() { viewType }
    @Deprecated void setType(String type) { this.viewType = type }
    // @formatter:on
}
