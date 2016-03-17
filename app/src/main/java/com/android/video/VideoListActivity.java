package com.android.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.tedcoder.wkvideoplayer.R;
import com.android.video.widget.MediaHelp;
import com.android.video.widget.VideoSuperPlayer;
import com.android.video.widget.VideoSuperPlayer.VideoPlayCallbackImpl;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends Activity {
	private String url = "http://2449.vod.myqcloud.com/2449_43b6f696980311e59ed467f22794e792.f20.mp4";
	private List<VideoBean> mList;
	private ListView mListView;
	private boolean isPlaying;
	private int indexPostion = -1;
	private MAdapter mAdapter;

	@Override
	protected void onDestroy() {
		MediaHelp.release();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		MediaHelp.resume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		MediaHelp.pause();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		MediaHelp.getInstance().seekTo(data.getIntExtra("position", 0));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.list);
		mList = new ArrayList<VideoBean>();
		for (int i = 0; i < 10; i++) {
			mList.add(new VideoBean(url));
		}
		mAdapter = new MAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if ((indexPostion < mListView.getFirstVisiblePosition() || indexPostion > mListView
						.getLastVisiblePosition()) && isPlaying) {
					indexPostion = -1;
					isPlaying = false;
					mAdapter.notifyDataSetChanged();
					MediaHelp.release();
				}
			}
		});
	}

	class MAdapter extends BaseAdapter {
		private Context context;
		LayoutInflater inflater;

		public MAdapter(Context context) {
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public VideoBean getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			GameVideoViewHolder holder = null;
			if (v == null) {
				holder = new GameVideoViewHolder();
				v = inflater.inflate(R.layout.list_video_item, parent, false);
				holder.mVideoViewLayout = (VideoSuperPlayer) v
						.findViewById(R.id.video);
				holder.mPlayBtnView = (ImageView) v.findViewById(R.id.play_btn);
				v.setTag(holder);
			} else {
				holder = (GameVideoViewHolder) v.getTag();
			}
			holder.mPlayBtnView.setOnClickListener(new MyOnclick(
					holder.mPlayBtnView, holder.mVideoViewLayout, position));
			if (indexPostion == position) {
				holder.mVideoViewLayout.setVisibility(View.VISIBLE);
			} else {
				holder.mVideoViewLayout.setVisibility(View.GONE);
				holder.mVideoViewLayout.close();
			}
			return v;
		}

		class MyOnclick implements OnClickListener {
			VideoSuperPlayer mSuperVideoPlayer;
			ImageView mPlayBtnView;
			int position;

			public MyOnclick(ImageView mPlayBtnView,
					VideoSuperPlayer mSuperVideoPlayer, int position) {
				this.position = position;
				this.mSuperVideoPlayer = mSuperVideoPlayer;
				this.mPlayBtnView = mPlayBtnView;
			}

			@Override
			public void onClick(View v) {
				MediaHelp.release();
				indexPostion = position;
				isPlaying = true;
				mSuperVideoPlayer.setVisibility(View.VISIBLE);
				mSuperVideoPlayer.loadAndPlay(MediaHelp.getInstance(), mList
						.get(position).getUrl(), 0, false);
				mSuperVideoPlayer.setVideoPlayCallback(new MyVideoPlayCallback(
						mPlayBtnView, mSuperVideoPlayer, mList.get(position)));
				notifyDataSetChanged();
			}
		}

		class MyVideoPlayCallback implements VideoPlayCallbackImpl {
			ImageView mPlayBtnView;
			VideoSuperPlayer mSuperVideoPlayer;
			VideoBean info;

			public MyVideoPlayCallback(ImageView mPlayBtnView,
					VideoSuperPlayer mSuperVideoPlayer, VideoBean info) {
				this.mPlayBtnView = mPlayBtnView;
				this.info = info;
				this.mSuperVideoPlayer = mSuperVideoPlayer;
			}

			@Override
			public void onCloseVideo() {
				closeVideo();
			}

			@Override
			public void onSwitchPageType() {
				if (((Activity) context).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					Intent intent = new Intent(new Intent(context,
							FullVideoActivity.class));
					intent.putExtra("video", info);
					intent.putExtra("position",
							mSuperVideoPlayer.getCurrentPosition());
					((Activity) context).startActivityForResult(intent, 1);
				}
			}

			@Override
			public void onPlayFinish() {
				closeVideo();
			}

			private void closeVideo() {
				isPlaying = false;
				indexPostion = -1;
				mSuperVideoPlayer.close();
				MediaHelp.release();
				mPlayBtnView.setVisibility(View.VISIBLE);
				mSuperVideoPlayer.setVisibility(View.GONE);
			}

		}

		class GameVideoViewHolder {

			private VideoSuperPlayer mVideoViewLayout;
			private ImageView mPlayBtnView;

		}

	}

}
