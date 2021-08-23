package com.txznet.loader;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaList;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.music.receiver.HeadSetHelper;
import com.txznet.music.receiver.HeadSetHelper.OnHeadSetListener;
import com.txznet.music.service.MusicService;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();

		ServiceManager.getInstance().keepConnection(ServiceManager.TXZ,
				new Runnable() {
					@Override
					public void run() {
						MediaItem item = MusicService.getInstance()
								.getCurModel();
						if (item != null) {
							ServiceManager.getInstance().sendInvoke(
									ServiceManager.TXZ,
									"txz.music.inner.musicModel",
									MessageNano.toByteArray(item), null);
						}
						ServiceManager
								.getInstance()
								.sendInvoke(
										ServiceManager.TXZ,
										"txz.music.inner.isPlaying",
										("" + (MusicService.getInstance()
												.isPlaying() || MusicService
												.getInstance().isLogicPaused()))
												.getBytes(), null);
					}
				});

		// 空列表则同步一次
		MediaList lst = MusicService.getInstance().getMediaList();
		MusicService.getInstance().refreshCategoryList();
		if (lst == null || lst.rptMediaItem == null
				|| lst.rptMediaItem.length == 0) {
			MusicService.getInstance().syncMusicList();
		}
		
		HeadSetHelper.getInstance().open(GlobalContext.get());
		HeadSetHelper.getInstance().setOnHeadSetListener(new OnHeadSetListener() {

			@Override
			public void onDoubleClick() {
			}

			@Override
			public void onClick() {
				if (MusicService.getInstance().isPlaying())
					MusicService.getInstance().pausePlay();
				else
					MusicService.getInstance().resumePlay();
			}
		});
		
	}
}
