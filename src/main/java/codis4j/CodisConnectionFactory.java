package codis4j;

import static org.apache.curator.framework.imps.CuratorFrameworkState.LATENT;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;

import codis4j.connection.BoundedExponentialBackoffRetryUntilElapsed;
import codis4j.connection.DirectCodisConnectionHandleImpl;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class CodisConnectionFactory {
	// coordinator_name, only accept "zookeeper" & "etcd" & "filesystem".
	private static final String ZOOKEEPER = "zookeeper";
	private static final String ETCD = "etcd";
	private static final String FILESYSTEM = "filesystem";
	private String coordinatorName;
	private String coordinatorAddr;
	private String productName;
	private String productAuth;
	private String clientName;

	private JedisPoolConfig poolConfig;
	private int connectionTimeoutMs = Protocol.DEFAULT_TIMEOUT;
	private int soTimeoutMs = Protocol.DEFAULT_TIMEOUT;
	private int db = Protocol.DEFAULT_DATABASE;

	// zookeeper
	private static final int CURATOR_RETRY_BASE_SLEEP_MS = 100;
	private static final int CURATOR_RETRY_MAX_SLEEP_MS = 30 * 1000;
	private String scheme;
	private String auth;
	private String zkDir;
	// etcd
	// filesystem

	public CodisConnectionFactory(String coordinatorName, String coordinatorAddr, String productName,
			String productAuth, String clientName) {
		this.coordinatorName = coordinatorName;
		this.coordinatorAddr = coordinatorAddr;
		this.productName = productName;
		this.productAuth = productAuth;
		this.clientName = clientName;
	}

	public CodisConnectionFactory setPoolConfig(JedisPoolConfig poolConfig, int connectionTimeoutMs, int soTimeoutMs,
			int db) {
		this.poolConfig = poolConfig;
		this.connectionTimeoutMs = connectionTimeoutMs;
		this.soTimeoutMs = soTimeoutMs;
		this.db = db;
		return this;
	}

	public CodisConnectionFactory setZKConfig(String scheme, String auth, String zkDir) {
		this.scheme = scheme;
		this.auth = auth;
		this.zkDir = zkDir;
		return this;
	}

	public CodisConnectionHandler connect() {
		switch (coordinatorName) {
		case ZOOKEEPER:
			return new ZKCodisConnectionBuilder().connect();
		case ETCD:
			return new ETCDCodisConnectionBuilder().connect();
		case FILESYSTEM:
			return new FSCodisConnectionBuilder().connect();
		default:
			return null;
		}
	}

	public final class ZKCodisConnectionBuilder implements CodisConnectionBuilder {
		private CuratorFramework curatorClient;

		@Override
		public DirectCodisConnectionHandleImpl connect() {
			if (this.curatorClient == null) {
				if (scheme != null && auth != null) {
					this.curatorClient = CuratorFrameworkFactory.builder().connectString(coordinatorAddr)
							.authorization(scheme, auth.getBytes())
							.retryPolicy(new BoundedExponentialBackoffRetryUntilElapsed(CURATOR_RETRY_BASE_SLEEP_MS,
									CURATOR_RETRY_MAX_SLEEP_MS, -1L))
							.build();
				} else {
					this.curatorClient = CuratorFrameworkFactory.builder().connectString(coordinatorAddr)
							.retryPolicy(new BoundedExponentialBackoffRetryUntilElapsed(CURATOR_RETRY_BASE_SLEEP_MS,
									CURATOR_RETRY_MAX_SLEEP_MS, -1L))
							.build();
				}
				this.curatorClient.start();
			} else {
				if (this.curatorClient.getState() == LATENT) {
					this.curatorClient.start();
				}
			}
			return new DirectCodisConnectionHandleImpl(curatorClient, zkDir, poolConfig, connectionTimeoutMs,
					soTimeoutMs, db, productName, productAuth, clientName);
		}
	}

	public final class ETCDCodisConnectionBuilder implements CodisConnectionBuilder {

		@Override
		public CodisConnectionHandler connect() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public final class FSCodisConnectionBuilder implements CodisConnectionBuilder {

		@Override
		public CodisConnectionHandler connect() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
