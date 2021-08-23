package com.txznet.audio.server.response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Environment;
import android.os.SystemClock;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.audio.player.audio.NetAudio;
import com.txznet.audio.player.audio.TmdFile;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;

/**
 * 临界值，数据段（读取/写入 块），
 * 
 * @author telenewbie
 * @version 2016年6月17日
 */
public class CacheInfo {
	File mCacheTmpFile = null;
	File mFinalFile = null;
	long mTotal = 0;
	SessionInfo mSession;
	private static final int sDefaultCacheSize = 1 * 1024 * 1024;// 默认缓存大小
	private static final long sDefaultSaveTime = 3000;// 默认落地时间
	private static final String TAG = "[MUSIC][Cache]";
	TmdFile mTmdFile = null;// 文件句柄
	Object mTmdHandlerLock = new Object();
	boolean mReleased = false;
	static Map<String, CacheInfo> memeryCache = new HashMap<String, CacheInfo>();// CacheId
	List<LocalBuffer> mCacheList = new ArrayList<LocalBuffer>();
	long mLastUseTime = SystemClock.elapsedRealtime();

	private void lastUse() {
		mLastUseTime = SystemClock.elapsedRealtime();
		AppLogic.removeBackGroundCallback(mRunnableRelease);
	}

	private CacheInfo(SessionInfo sess, long total) {
		NetAudio audio = (NetAudio) sess.audio;
		String cacheId = audio.getCacheId();

		if (Constant.ISTEST) {
			LogUtil.logd("media session[" + sess.getLogId() + "] cache["
					+ cacheId + "] for url: " + audio.getUrl());
		}

		mCacheTmpFile = new File(audio.getCacheDir(), cacheId + ".tmp");
		mFinalFile = audio.getFinalFile();
		mSession = sess;
		mTotal = total;

		loadCache();
	}

	public static CacheInfo createCacheInfo(SessionInfo sess, long total) {
		NetAudio audio = (NetAudio) sess.audio;
		String cacheId = audio.getCacheId();
		File cache = new File(audio.getCacheDir(), cacheId + ".tmp");
		File finalFile = audio.getFinalFile();
		CacheInfo ret = null;

		synchronized (memeryCache) {
			try {
				for (Entry<String, CacheInfo> entry : memeryCache.entrySet()) {
					entry.getValue().releaseDelay();
				}

				if (null != memeryCache.get(cacheId)) {
					ret = memeryCache.get(cacheId);
					return ret;
				}

				if (finalFile != null && finalFile.exists()) {
					if (total < 0) {
						total = TmdFile.getDataSize(finalFile);
						if (total > 0) {
							cache.delete();
							ret = new CacheInfo(sess, total);
							memeryCache.put(cacheId, ret);
							return ret;
						}
					} else {
						// TODO checkFile
						ret = new CacheInfo(sess, total);
						memeryCache.put(cacheId, ret);
						return ret;
					}
					finalFile.delete();
				}

				if (total < 0) {
					total = TmdFile.getDataSize(cache);
					if (total > 0) {
						ret = new CacheInfo(sess, total);
						memeryCache.put(cacheId, ret);
						return ret;
					}
				} else {
					ret = new CacheInfo(sess, total);
					memeryCache.put(cacheId, ret);
					return ret;
				}
			} finally {
				if (ret != null) {
					ret.lastUse();
				}
			}
		}
		if (null == ret) {
			LogUtil.logd(TAG + "cacheinfo " + cacheId);
		}

		return ret;

	}

	/**
	 * 装载缓存
	 */
	private void loadCache() {
		mCacheList.clear();
		if (Constant.ISTESTDATA) {
			LogUtil.logd("-----------------1" + mFinalFile);
		}

		if (mFinalFile != null) {
			if (mFinalFile.exists()) {
				if (TmdFile.checkFile(mFinalFile, mTotal)) {
					LogUtil.logd("media session[" + mSession.getLogId()
							+ "] load cache from final file: "
							+ mFinalFile.getPath());

					rebuildTempCache(true);
					if (Constant.ISTESTDATA) {
						LogUtil.logd("-----------------1---1" + mFinalFile);
					}
					mCacheList.add(LocalBuffer.buildFull(mTotal));

					mTmdFile = TmdFile.openFile(mFinalFile, mTotal);

					return;
				}
				LogUtil.logw("media session[" + mSession.getLogId()
						+ "]check final file failed: " + mFinalFile.getPath());
				mFinalFile.delete();
			}
		}
		if (Constant.ISTESTDATA) {
			LogUtil.logd("-----------------2" + mFinalFile);
		}

		if (TmdFile.checkFile(mCacheTmpFile)) {
			byte[] info = TmdFile.loadInfo(mCacheTmpFile, mTotal);
			List<LocalBuffer> buffers = LocalBuffer.buildList(info);
			if (buffers != null) {
				mCacheList = buffers;
			}
		}
		new Thread() {
			@Override
			public void run() {
				TmdFile f = TmdFile.openFile(mCacheTmpFile, mTotal);// 创建文件
				if (f == null) {
					rebuildTempCache();
					f = TmdFile.openFile(mCacheTmpFile, mTotal);
				}
				mTmdFile = f;
			}
		}.start();
		return;
	}

	/**
	 * 保存缓存
	 */
	private void saveCache() {
		if (mFinalFile != null
				&& mTmdFile.getFile().getAbsolutePath()
						.equals(mFinalFile.getAbsolutePath())) {
			return;
		}

		// TODO buffer区间合并
		for (int i = 0; i < mCacheList.size() - 1;) {
			if (mCacheList.get(i + 1).getFrom() == mCacheList.get(i).getTo() + 1
					&& mCacheList.get(i).getBufferData() == null
					&& mCacheList.get(i + 1).getBufferData() == null) {
				mCacheList.get(i + 1).setFrom(mCacheList.get(i).getFrom());
				mCacheList.remove(i);
				continue;
			}
			++i;
		}

		byte[] info = LocalBuffer.toBytes(mCacheList);
		if (Constant.ISTESTDATA) {
			LogUtil.logd("mCacheTmpFile="
					+ mCacheTmpFile.getName()
					+ ",mTotal="
					+ mTotal
					+ ",info="
					+ (mCacheList.size() > 0 ? mCacheList.get(0).getFrom() : ""));
		}
		if (mTmdFile.saveInfo(info) == false) {
			LogUtil.logw("media session[" + mSession.getLogId()
					+ "]save cache index failed size: " + info.length);
			// rebuildTempCache(false);
		}

		// 判断是否已经下载完整
		int downloadSize = 0;

		synchronized (mCacheList) {
			for (int i = 0; i < mCacheList.size(); i++) {
				LocalBuffer buffer = mCacheList.get(i);
				if (buffer != null) {
					downloadSize += buffer.getTo() - buffer.getFrom() + 1;
				}
			}
		}

		if (downloadSize >= mTotal) {
			LogUtil.logi("media session[" + mSession.getLogId()
					+ "]download cache data complete");

			NetAudio audio = (NetAudio) mSession.audio;
			if (mFinalFile != null) {
				try {
					mTmdFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mFinalFile.getParentFile().mkdirs();
				mCacheTmpFile.renameTo(mFinalFile);
				mTmdFile = TmdFile.openFile(mFinalFile, mTotal);
				mTmdFile.saveInfo(audio.getAudioInfo());
			}
			audio.onDownloadComplete();
		}
		// if (mCacheList.size() == 1) {
		// LocalBuffer buffer = mCacheList.get(0);
		// if (buffer.getFrom() == 0 && buffer.getTo() == mTotal - 1) {
		// }
		// }

	}

	void debugCacheList(String prefix) {
		LogUtil.logd("media session[" + mSession.getLogId() + "]" + prefix
				+ ": cache count=" + mCacheList.size());
		for (int i = 0; i < mCacheList.size(); ++i) {
			LocalBuffer buffer = mCacheList.get(i);
			LogUtil.logd("media session[" + mSession.getLogId() + "]" + prefix
					+ ": [" + buffer.getFrom() + "~" + buffer.getTo() + "]|"
					+ mTotal);
		}
	}

	private byte[] copyBufferData(byte[] data, int offset, int len, boolean need) {
		if (!need) {
			return data;
		}
		byte[] tmpdata = new byte[len < LocalBuffer.MIN_BUFFER_SIZE ? LocalBuffer.MIN_BUFFER_SIZE
				: len];
		System.arraycopy(data, offset, tmpdata, 0, len);
		return tmpdata;
	}

	/**
	 * 合并缓存计算，先不管crc32
	 */
	private void mergeCache(long from, long to, byte[] data, int offset, int len) {
		if (Constant.ISTESTDATA) {
			LogUtil.logd("media session[" + mSession.getLogId()
					+ "] merge cache: [" + from + "~" + to + "]|" + mTotal);
		}

		boolean needCopyData = true;
		byte[] newdata = null;

		int i = 0;
		while (i < mCacheList.size()) {

			LocalBuffer buffer = mCacheList.get(i);
			if (from < buffer.getFrom()) {
				// from在当前block左侧
				if (to < buffer.getFrom() - 1) {// 不相交
					break; // 在当前block左侧，完全无法叠加，跳出插入到当前位置
				} else if (to >= buffer.getTo()) {// 包含
					mCacheList.remove(i); // 新插入的block大于当前block，抛弃当前block，与下一块比较
					continue;
				} else {// 交集
					// 有数据和没有数据进行合并生成两个字段
					if (null != buffer.getBufferData()) {// 该段也是有数据的则直接进行合并
						// 拼起来重复块
						int newlen = (int) (buffer.getTo() - from) + 1;
						if (needCopyData == false && data.length >= newlen) {
							newdata = data;
						} else {
							newdata = new byte[newlen < LocalBuffer.MIN_BUFFER_SIZE ? LocalBuffer.MIN_BUFFER_SIZE
									: newlen];
							System.arraycopy(data, offset, newdata, 0,
									(int) (to - from + 1));
						}
						System.arraycopy(buffer.getBufferData(), (int) (to
								- buffer.getFrom() + 1), newdata, (int) (to
								- from + 1), newlen - ((int) (to - from + 1)));

						buffer.setFrom(from); // 更新当前block
						buffer.setBufferData(newdata);// 截断一部分数据
						writeTestData("merge", from, buffer.getTo(), newdata,
								offset);
						return;
					} else {
						buffer.setFrom(to + 1);
						break;
					}
				}
			} else if (from > buffer.getTo() + 1) {
				++i;
				continue; // from在当前block右侧，下一块处理
			} else {
				if (to <= buffer.getTo()) {
					if (null != buffer.getBufferData()) {
						// 更新老的数据
						System.arraycopy(data, offset, buffer.getBufferData(),
								(int) (from - buffer.getFrom()), (int) (to
										- from + 1));
					} else {
						if (from == buffer.getFrom()) {
							if (to == buffer.getTo()) {
								buffer.setBufferData(copyBufferData(data,
										offset, len, needCopyData));
								writeTestData("update", from, to, data, offset);
								return;
							} else {
								buffer.setFrom(to + 1);
								break;
							}
						} else {
							if (to == buffer.getTo()) {
								buffer.setTo(from - 1);
								++i;
								break;
							} else {
								buffer.setFrom(to + 1);
								mCacheList.add(i, LocalBuffer.build(
										buffer.getFrom(), from - 1,
										buffer.getTotal(), LocalBuffer.CRC32,
										null));
								break;
							}
						}
					}
					return; // 当前block已经包含了from~to
				} else {
					if (null == buffer.getBufferData()) {
						++i;
						buffer.setTo(from - 1);
					} else {
						int newlen = (int) (to - buffer.getFrom() + 1);
						if (buffer.getBufferData().length >= newlen) {
							newdata = buffer.getBufferData();
						} else {
							newdata = new byte[newlen < LocalBuffer.MIN_BUFFER_SIZE ? LocalBuffer.MIN_BUFFER_SIZE
									: newlen];
							System.arraycopy(buffer.getBufferData(), 0,
									newdata, 0, (int) (from - buffer.getFrom()));
						}
						System.arraycopy(data, offset, newdata,
								(int) (from - buffer.getFrom()), (int) (to
										- from + 1));
						data = newdata;
						offset = 0;
						len = newlen;
						needCopyData = false;
						// from可以和当前block衔接
						mCacheList.remove(i);
						from = buffer.getFrom();
					}
					continue;
				}
			}
		}
		writeTestData("insert", from, to, data, offset);
		mCacheList.add(i, LocalBuffer.build(from, to, mTotal,
				LocalBuffer.CRC32,
				copyBufferData(data, offset, len, needCopyData)));
	}

	private void writeTestData(String type, long from, long to, byte[] data,
			int offset) {
		// 测试数据
		if (Constant.ISTEST) {
			File file = new File(Environment.getExternalStorageDirectory(),
					"txz/audio/mem/" + ((NetAudio) mSession.audio).getCacheId()
							+ "_" + type + "_" + from + "_" + to + ".dat");
			file.getParentFile().mkdirs();
			FileOutputStream fosMemCache = null;
			try {
				fosMemCache = new FileOutputStream(file);
				fosMemCache.write(data, offset, (int) (to - from + 1));
				fosMemCache.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fosMemCache != null) {
					try {
						fosMemCache.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void rebuildTempCache() {
		rebuildTempCache(true);
	}

	private void rebuildTempCache(boolean cleanList) {
		if (cleanList) {
			synchronized (mCacheList) {
				mCacheList.clear();
			}
		}
		mCacheTmpFile.delete();
	}

	/**
	 * 重置缓存
	 */
	protected void rebuild() {
		mFinalFile.delete();
		rebuildTempCache();
	}

	/**
	 * 更新缓存数据
	 */
	public boolean addCacheBlock(long from, byte[] data, int offset, int len) {
		lastUse();

		if (len == 0) {
			return true;
		}
		long to = from + len - 1;
		if (from >= mTotal || to >= mTotal) {
			LogUtil.loge("media session[" + mSession.getLogId()
					+ "]write cache data too long: from=" + from + ", len="
					+ len + ", total=" + mTotal);
			return false;
		}
		long startTime = System.currentTimeMillis();

		// debugCacheList("before merge cache");
		synchronized (mCacheList) {
			mergeCache(from, to, data, offset, len);
			if (null != mCacheList && mCacheList.size() > 0) {
				int cacheTotalSize = 0;
				for (int i = 0; i < mCacheList.size(); i++) {
					if (null != mCacheList.get(i).getBufferData()) {
						cacheTotalSize += mCacheList.get(i).getTo()
								- mCacheList.get(i).getFrom() + 1;
					}
				}
				AppLogic.removeBackGroundCallback(mRunnableWriteCacheFile);
				if (cacheTotalSize > sDefaultCacheSize) {
					AppLogic.runOnBackGround(mRunnableWriteCacheFile, 0);
				} else {
					AppLogic.runOnBackGround(mRunnableWriteCacheFile,
							sDefaultSaveTime);
				}
			}
		}
		// debugCacheList("after merge cache");

		if (Constant.ISTESTDATA) {
			LogUtil.logd("mergeCache use time:"
					+ (System.currentTimeMillis() - startTime));
		}

		return true;
	}

	// // 当data超过3s就落地文件，当文件大小超过100K就落地文件
	Runnable mRunnableWriteCacheFile = new Runnable() {
		@Override
		public void run() {
			lastUse();

			synchronized (mTmdHandlerLock) {
				if (mTmdFile == null) {
					AppLogic.runOnBackGround(this, sDefaultSaveTime);
					return;
				}
				List<LocalBuffer> tmpList = new ArrayList<LocalBuffer>();
				synchronized (mCacheList) {
					for (int i = 0; i < mCacheList.size(); i++) {
						LocalBuffer cache = mCacheList.get(i);
						if (cache.getBufferData() != null) {
							if (mTmdFile
									.saveDataBlock(
											cache.getFrom(),
											cache.getBufferData(),
											0,
											(int) (cache.getTo()
													- cache.getFrom() + 1))) {
								cache.setBufferData(null);// 清空内存数据
							} else {
								// TODO 缓存数据保存失败
							}
						}
					}

					saveCache();
				}
			}
		}
	};

	public List<LocalBuffer> getCacheBlocks() {
		lastUse();

		List<LocalBuffer> buffers = new ArrayList<LocalBuffer>();
		synchronized (mCacheList) {
			// 剔除掉data字段的数据
			if (mCacheList != null && mCacheList.size() > 0) {
				for (int i = 0; i < mCacheList.size(); i++) {
					buffers.add(LocalBuffer.build(mCacheList.get(i).getFrom(),
							mCacheList.get(i).getTo(), mCacheList.get(i)
									.getTotal(), mCacheList.get(i).getCrc32()));
				}
			}
		}
		return buffers;
	}

	public long getTotalSize() {
		lastUse();
		return mTotal;
	}

	public static class CacheData {
		public byte[] data = null;
		public int len = 0;
		public long next = -1;
	}

	public boolean getCacheData(CacheData data, long from, long to,
			boolean greedy) {
		lastUse();

		synchronized (mTmdHandlerLock) {

			if (Constant.ISTESTDATA) {
				LogUtil.logd("media session[" + mSession.getLogId()
						+ "]write cache data: from=" + from + ", to=" + to
						+ ", cacheSize=" + mCacheList.size());
			}
			synchronized (mCacheList) {
				int i = 0;
				data.len = 0; // 默认没有取到数据
				data.next = -1; // 默认没有下一块
				for (; i < mCacheList.size(); ++i) {
					LocalBuffer buffer = mCacheList.get(i);
					// 左边无叠加，不存在
					if (to < buffer.getFrom()) {
						data.next = buffer.getFrom();
						return false;
					}
					// from处于该块中
					if (from >= buffer.getFrom() && from <= buffer.getTo()) {
						int endIndex = i;
						for (int j = i; j < mCacheList.size(); ++j) {
							if (j > i
									&& mCacheList.get(j).getFrom() != mCacheList
											.get(j - 1).getTo() + 1) {
								break;
							}
							if (j < mCacheList.size() - 1) {
								data.next = mCacheList.get(j + 1).getFrom();
							} else {
								data.next = -1;
							}
							if (greedy && to <= mCacheList.get(j).getTo()) {
								to = mCacheList.get(j).getTo();
							}
							endIndex = j;
						}

						if (to > mCacheList.get(endIndex).getTo()) {
							to = mCacheList.get(endIndex).getTo();
						}

						data.len = (int) (to - from + 1);
						if (data.data == null
								|| to - from + 1 > data.data.length) {
							data.data = new byte[data.len < LocalBuffer.MIN_BUFFER_SIZE ? LocalBuffer.MIN_BUFFER_SIZE
									: data.len];
						}

						int offset = 0;
						for (int j = i; j <= endIndex; ++j) {
							int l = (int) (mCacheList.get(j).getTo()
									- mCacheList.get(j).getFrom() + 1);
							if (offset + l > data.len) {
								l = data.len - offset;
							}
							if (l <= 0)
								break;
							if (mCacheList.get(j).getBufferData() != null) {
								System.arraycopy(mCacheList.get(j)
										.getBufferData(), 0, data.data, offset,
										l);
							} else {
								boolean readSucc = false;
								try {
									readSucc = mTmdFile.readData(data.data,
											offset, (int) from, l);
								} catch (Exception e) {
									LogUtil.logw("media session["
											+ mSession.getLogId()
											+ "]read excepiton: "
											+ e.getMessage());
								}
								if (!readSucc) {
									writeTestData("read_error", from, from
											+ offset - 1, data.data, 0);
									data.len = offset;
									return true;
								}
							}
							writeTestData("read_piece", from + offset, from
									+ offset + l - 1, data.data, offset);
							offset += l;
						}

						writeTestData("read", from, to, data.data, 0);

						return true;
					}
				}
			}
		}
		return true;
	}

	private void releaseDelay() {
		AppLogic.removeBackGroundCallback(mRunnableRelease);
		AppLogic.runOnBackGround(mRunnableRelease, 5000);
	}

	private Runnable mRunnableRelease = new Runnable() {
		@Override
		public void run() {
			// 缓存文件没有创建好，等待5秒后再释放
			if (mTmdFile == null) {
				releaseDelay();
				return;
			}

			// 缓存数据还有没落地的，等待5秒后再释放
			synchronized (mTmdHandlerLock) {
				for (int i = 0; i < mCacheList.size(); ++i) {
					if (mCacheList.get(i).getBufferData() != null) {
						releaseDelay();
						return;
					}
				}
			}

			NetAudio audio = (NetAudio) mSession.audio;
			String cacheId = audio.getCacheId();
			synchronized (memeryCache) {
				// 缓存最后3秒内有被使用
				if (SystemClock.elapsedRealtime() - mLastUseTime < 3000) {
					releaseDelay();
					return;
				}

				memeryCache.remove(cacheId);
				try {
					mTmdFile.close();
				} catch (IOException e) {
				}
				mTmdFile = null;
			}

			LogUtil.logd("media session[" + mSession.getLogId()
					+ "]release global cache object");
		}
	};
}
