package lmc.danielcain;

public class Label {
    private String label;
    private int address;

    public Label(String label, int address) {
        this.label = label;
        this.address = address;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }
}
