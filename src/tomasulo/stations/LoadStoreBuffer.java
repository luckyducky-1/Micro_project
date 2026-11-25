package tomasulo.stations;

public class LoadStoreBuffer extends ReservationStation {
    int offset;
    long issueOrder;
    LoadStoreBuffer(String tag) {
        super(tag);
    }
    @Override
    public void clear() {
        super.clear();
        offset = 0;
        issueOrder = 0;
    }

    Long effectiveAddress() {
        if (qj != null || vj == null) {
            return null;
        }

        return vj.longValue() + offset;
    }
    private boolean isStore() {
        return op != null && op.startsWith("S");
    }

    boolean canAccessMemory(LoadStoreBuffer... buffers) {
        return canAccessMemory(java.util.Arrays.asList(buffers));
    }

    boolean canAccessMemory(Iterable<LoadStoreBuffer> buffers) {
        Long address = effectiveAddress();
        if (!busy || address == null) {
            return false;
        }

        if (isStore() && (qk != null || vk == null)) {
            return false;
        }

        for (LoadStoreBuffer older : buffers) {
            if (!older.busy || older.issueOrder >= issueOrder) {
                continue;
            }

            if (!isStore() && !older.isStore()) {
                continue;
            }

            Long olderAddress = older.effectiveAddress();
            if (olderAddress == null || address.equals(olderAddress)) {
                return false;
            }
        }

        return true;
    }
    public static void main(String[] args) {
        LoadStoreBuffer store = new LoadStoreBuffer("Store1");
        store.busy = true;
        store.op = "S.D";
        store.issueOrder = 1;
        store.vj = 32L;
        store.vk = 5.0;

        LoadStoreBuffer load = new LoadStoreBuffer("Load1");
        load.busy = true;
        load.op = "L.D";
        load.issueOrder = 2;
        load.vj = 32L;

        System.out.println("Same address: " + load.canAccessMemory(store));

        load.offset = 8;
        System.out.println("Different addresses: " + load.canAccessMemory(store));

        load.offset = 0;
        store.busy = false; // Simulate the older store completing.
        System.out.println("After store completes: " + load.canAccessMemory(store));
    }
}