package ct26

import acmi.l2.clientmod.util.Type
import groovy.beans.Bindable
import groovy.transform.CompileDynamic

@Bindable
@CompileDynamic
class ScrollArea extends DefaultProperty implements Iterable<DefaultProperty> {
    int areaHeight
    @Type(DefaultProperty.class)
    List<DefaultProperty> children = []

    @Override
    Iterator<DefaultProperty> iterator() {
        children.iterator()
    }

    @Override
    ScrollArea read(InputStream input) {
        super.read(input)

            areaHeight = input.readInt()
            children = input.readList(DefaultProperty)

        this
    }

    @Override
    ScrollArea write(OutputStream output) {
        super.write(output)

            output.writeInt(areaHeight)
            output.writeInt(children.size())
            for (DefaultProperty child : children)
                output.writeUIEntity(child)

        this
    }

    // @formatter:off
    @Deprecated int getUnk100() { areaHeight }
    @Deprecated void setUnk100(int unk100) { this.areaHeight = unk100 }
    // @formatter:on
}
