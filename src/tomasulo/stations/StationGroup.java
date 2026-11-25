package tomasulo.stations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class StationGroup<T extends ReservationStation> {
    private final List<T> stations = new ArrayList<>();

    public StationGroup(String prefix, int size, Function<String, T> createStation) {
        if (size < 1) {
            throw new IllegalArgumentException("Group size must be at least 1.");
        }

        for (int i = 1; i <= size; i++) {
            stations.add(createStation.apply(prefix + i));
        }
    }

    public T findFreeStation() {
        for (T station : stations) {
            if (!station.busy) {
                return station;
            }
        }
        return null;
    }

    public List<T> entries() {
        return Collections.unmodifiableList(stations);
    }

    public static void main(String[] args) {
        StationGroups groups = new StationGroups(2, 1, 3, 2, 1);
        StationGroup<ReservationStation> addStations = groups.fpAddStations;
        StationGroup<LoadStoreBuffer> loadBuffers = groups.loadBuffers;
        StationGroup<LoadStoreBuffer> storeBuffers = groups.storeBuffers;

        System.out.println("First add station: " + addStations.findFreeStation().tag);

        LoadStoreBuffer firstLoad = loadBuffers.findFreeStation();
        System.out.println("First load buffer: " + firstLoad.tag);
        firstLoad.busy = true;
        System.out.println("Next load buffer: " + loadBuffers.findFreeStation().tag);

        LoadStoreBuffer firstStore = storeBuffers.findFreeStation();
        System.out.println("First store buffer: " + firstStore.tag);
        firstStore.busy = true;

        firstLoad.op = "L.D";
        firstLoad.issueOrder = 1;
        firstLoad.vj = 32L;

        firstStore.op = "S.D";
        firstStore.issueOrder = 2;
        firstStore.vj = 32L;
        firstStore.vk = 5.0;

        boolean storeCanProceed = groups.canStartMemoryAccess(firstStore);
        System.out.println("Store can proceed at the same address: " + storeCanProceed);

        firstStore.offset = 8;
        storeCanProceed = groups.canStartMemoryAccess(firstStore);
        System.out.println("Store can proceed at a different address: " + storeCanProceed);
        System.out.println("Store group is full: " + (storeBuffers.findFreeStation() == null));

        firstStore.clear();

        LoadStoreBuffer reusableStore = storeBuffers.findFreeStation();
        System.out.println("Free store buffer: " + reusableStore.tag);
        System.out.println("Old address cleared: " + (reusableStore.effectiveAddress() == null));
        System.out.println("Offset reset: " + (reusableStore.offset == 0));

        // Reuse Store1 for an older store whose base register is pending.
        reusableStore.busy = true;
        reusableStore.op = "S.D";
        reusableStore.issueOrder = 3;
        reusableStore.qj = "Int1";
        reusableStore.vk = 7.0;

        // Reuse Load1 for a younger load targeting address 32.
        firstLoad.clear();
        firstLoad.busy = true;
        firstLoad.op = "L.D";
        firstLoad.issueOrder = 4;
        firstLoad.vj = 32L;

        System.out.println("Load can start before store address is known: "
                + groups.canStartMemoryAccess(firstLoad));

        reusableStore.vj = 40L;
        reusableStore.qj = null;

        System.out.println("Load can start after store address resolves to 40: "
                + groups.canStartMemoryAccess(firstLoad));
    }
}