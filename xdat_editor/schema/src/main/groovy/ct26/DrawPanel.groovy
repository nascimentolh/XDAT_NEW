package ct26

import acmi.l2.clientmod.util.defaultio.DefaultIO
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@DefaultIO
@CompileDynamic
class DrawPanel extends DefaultProperty {
    Boolean autoSize
}
