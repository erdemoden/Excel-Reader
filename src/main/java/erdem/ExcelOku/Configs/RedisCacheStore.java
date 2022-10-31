package erdem.ExcelOku.Configs;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class RedisCacheStore {

    private final RedisTemplate template;

    public <T> T get(final Object key, Class<T> clazz) {
        return (T) template.opsForValue().get(key);
    }

    public void put(final Object key, final Object value) {
        put(key, value, 0);
    }

    public void put(final Object key, final Object value, final long expiration) {
        template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.opsForValue().set(key, value);
                if (expiration > 0) {
                    operations.expire(key, expiration, TimeUnit.SECONDS);
                }
                return null;
            }
        });
    }

    public void put(final Object key, final Object value, final long expiration, final TimeUnit timeUnit) {
        template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.opsForValue().set(key, value);
                if (expiration > 0) {
                    operations.expire(key, expiration, timeUnit);
                }
                return null;
            }
        });
    }

    public void evict(final Object key) {
        template.delete(key);
    }

    public void evictByPrefix(final String prefix) {
        template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                Set<Object> keys = template.keys(prefix + "*");
                if (keys.size() == 0) {
                    return 0;
                }
                template.delete(keys);
                return keys.size();
            }
        });
    }

    public void clear() {
        template.execute((RedisCallback) connection -> {
            connection.flushAll();
            return true;
        });
    }

    public Iterable<String> keys(final String key) {
        return template.keys(key);
    }
}

