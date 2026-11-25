package tomasulo.stations;

public class StationGroups {
    public final StationGroup<ReservationStation> fpAddStations;
    public final StationGroup<ReservationStation> fpMulDivStations;
    public final StationGroup<ReservationStation> integerStations;
    public final StationGroup<LoadStoreBuffer> loadBuffers;
    public final StationGroup<LoadStoreBuffer> storeBuffers;

    public StationGroups(int fpAddCount, int fpMulDivCount, int integerCount,
                         int loadCount, int storeCount) {
        fpAddStations = new StationGroup<>("Add", fpAddCount, ReservationStation::new);
        fpMulDivStations = new StationGroup<>("Mul", fpMulDivCount, ReservationStation::new);
        integerStations = new StationGroup<>("Int", integerCount, ReservationStation::new);
        loadBuffers = new StationGroup<>("Load", loadCount, LoadStoreBuffer::new);
        storeBuffers = new StationGroup<>("Store", storeCount, LoadStoreBuffer::new);
    }
    public boolean canStartMemoryAccess(LoadStoreBuffer buffer) {
        return buffer.canAccessMemory(loadBuffers.entries())
                && buffer.canAccessMemory(storeBuffers.entries());
    }

    public static void main(String[] args) {
        StationGroups groups = new StationGroups(2, 1, 3, 2, 1);

        System.out.println("FP add/sub stations: " + groups.fpAddStations.entries().size());
        System.out.println("FP mul/div stations: " + groups.fpMulDivStations.entries().size());
        System.out.println("Integer stations: " + groups.integerStations.entries().size());
        System.out.println("Load buffers: " + groups.loadBuffers.entries().size());
        System.out.println("Store buffers: " + groups.storeBuffers.entries().size());
    }
}