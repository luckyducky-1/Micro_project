package tomasulo.stations;

public class ReservationStation {
    final String tag;
    boolean busy;
    String op;

    Number vj;
    Number vk;
    String qj;
    String qk;

    ReservationStation(String tag) {
        this.tag = tag;
    }
    public void clear() {
        busy = false;
        op = null;
        vj = null;
        vk = null;
        qj = null;
        qk = null;
    }
    public static void main(String[] args) {
        ReservationStation station = new ReservationStation("Add1");

        station.busy = true;
        station.op = "ADD.D";
        station.vj = 2.5;
        station.qk = "Mul1";

        System.out.println(station.tag + " holds " + station.op);
        System.out.println("First operand is ready: " + station.vj);
        System.out.println("Second operand is waiting for: " + station.qk);
    }
}
