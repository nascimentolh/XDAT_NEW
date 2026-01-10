package etoa3_ru

import acmi.l2.clientmod.util.defaultio.DefaultIO
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@DefaultIO
@CompileDynamic
class FlashCtrl extends DefaultProperty {
    String flashFile
}
