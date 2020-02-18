package lmc.danielcain;


public abstract class AbstractInstruction implements LittleManComputer.Instruction {
    protected LittleManComputer lmc;
    protected int opcode = 0;
    protected int address = 0;
    protected String mnemonic;

    public AbstractInstruction(LittleManComputer lmc, int opcode, String mnemonic) {
        this.mnemonic = mnemonic;
        this.opcode = opcode;
        this.lmc = lmc;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public boolean matches(int mailboxValue) {
        return (mailboxValue / 100) == (opcode / 100);
    }

    @Override
    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public void decode(int mailboxValue) {
        address = mailboxValue % 100;
    }

    @Override
    public int getOpCode() {
        return opcode;
    }

    @Override
    public int getAddress() {
        return address;
    }
}
