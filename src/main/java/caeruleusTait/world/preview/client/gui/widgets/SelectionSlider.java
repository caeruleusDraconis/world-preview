package caeruleusTait.world.preview.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SelectionSlider<T extends SelectionSlider.SelectionValues> extends AbstractSliderButton {
    private final List<T> values;
    private final Consumer<T> onValueChange;
    private T currentValue;

    public SelectionSlider(int x, int y, int width, int height, List<T> values, T initialValue, Consumer<T> onValueChange) {
        super(x, y, width, height, initialValue.message(), 0);
        this.values = values;
        this.onValueChange = onValueChange;
        setValue(initialValue);
    }

    public void setValue(T newValue) {
        if (Objects.equals(currentValue, newValue)) {
            onValueChange.accept(newValue);
        }
        currentValue = newValue;
        value = (double) values.indexOf(newValue) / (double) (values.size() - 1);
        updateMessage();
    }

    public T value() {
        return currentValue;
    }

    @Override
    protected void updateMessage() {
        setMessage(currentValue.message());
    }

    @Override
    protected void applyValue() {
        T oldValue = currentValue;
        currentValue = values.get((int) (value * (values.size() - 1)));
        if (!Objects.equals(oldValue, currentValue)) {
            onValueChange.accept(currentValue);
        }
    }

    public interface SelectionValues {
        Component message();
    }
}
