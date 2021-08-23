package com.txznet.comm.util.imageloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.txznet.txz.util.UrlUtil;
import com.txznet.txz.util.UrlUtil.ConnectInfo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class ImageLoaderInitialize {
	protected static DisplayImageOptions sDisplayImageOptions;
	public static DisplayImageOptions.Builder sDisplayBuilder;
	public static DisplayImageOptions mDefaultImageOptions;
	public static ImageLoaderConfiguration sImageLoaderConfiguration;
	public static MemoryCache mMemoryCache;

	public static void initImageLoader(Context application) {
		if (ImageLoaderImpl.getInstance().isInited() && ImageLoader.getInstance().isInited()) {
			return;
		}
		sDisplayBuilder = new DisplayImageOptions.Builder();
		mDefaultImageOptions = sDisplayBuilder.resetViewBeforeLoading(false).bitmapConfig(Bitmap.Config.RGB_565)
				.cacheInMemory(false).cacheOnDisk(false).build();

		ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(application)
				.defaultDisplayImageOptions(mDefaultImageOptions).imageDownloader(new BaseImageDownloader(application) {
					@Override
					protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
						HttpURLConnection connect = super.createConnection(url, extra);
						if (extra != null && extra instanceof ConnectInfo) {
							ConnectInfo info = (ConnectInfo) extra;
							Map<String, String> headMap = info.headers;
							if (headMap != null) {
								Set<Entry<String, String>> set = headMap.entrySet();
								for (Entry<String, String> entry : set) {
									connect.addRequestProperty(entry.getKey(), entry.getValue());
								}
								connect.addRequestProperty("test", "test");
							}
						}
						return connect;
					}

					@Override
					protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
						return super.getStreamFromNetwork(imageUri, extra);
					}
				}).memoryCache(mMemoryCache).tasksProcessingOrder(QueueProcessingType.LIFO)
				.threadPriority(Thread.NORM_PRIORITY - 2).threadPoolSize(1);
		sImageLoaderConfiguration = configBuilder.build();
		ImageLoader.getInstance().init(sImageLoaderConfiguration);
		ImageLoaderImpl.getInstance().init(sImageLoaderConfiguration);
	}

	public static class ImageLoaderImpl extends ImageLoader {

		private static ImageLoaderImpl sImpl = new ImageLoaderImpl();

		public static ImageLoaderImpl getInstance() {
			return sImpl;
		}

		@Override
		public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
			ConnectInfo connInfo = UrlUtil.parseUrl(uri);
			if (sDisplayBuilder != null) {
				sDisplayBuilder.extraForDownloader(connInfo);
				sDisplayBuilder.cacheOnDisk(true);
				init(sImageLoaderConfiguration);
			}
			super.displayImage(connInfo.url, imageView, sDisplayBuilder.build(), listener);
		}
	}
}
