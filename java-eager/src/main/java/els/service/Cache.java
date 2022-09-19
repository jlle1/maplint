package els.service;


import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Cache maps a file + an arbitrary key to a value. When the file is modified, the mapping expires. */
public class Cache<K, V> {
    private static class Key<K> {
        final Path file;
        final K key;

        Key(Path file, K key) {
            this.file = file;
            this.key = key;
        }

        @Override
        public boolean equals(Object other) {
            if (other.getClass() != Key.class) return false;
            var that = (Key) other;
            return Objects.equals(this.key, that.key) && Objects.equals(this.file, that.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, key);
        }
    }

    private class Value {
        final V value;
        final Instant created = Instant.now();

        Value(V value) {
            this.value = value;
        }
    }

    private final Map<Key, Value> map = new HashMap<>();

    public boolean has(Path file, K k) {
        return !needs(file, k);
    }

    public boolean needs(Path file, K k) {
        // If key is not in map, it needs to be loaded
        var key = new Key<K>(file, k);
        if (!map.containsKey(key)) return true;

        // If key was loaded before file was last modified, it needs to be reloaded
        var value = map.get(key);
        // TODO remove all keys associated with file when file changes
        return true;
    }

    public void load(Path file, K k, V v) {
        // TODO limit total size of cache
        var key = new Key<K>(file, k);
        var value = new Value(v);
        map.put(key, value);
    }

    public V get(Path file, K k) {
        var key = new Key<K>(file, k);
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException(k + " is not in map " + map);
        }
        return map.get(key).value;
    }
}
