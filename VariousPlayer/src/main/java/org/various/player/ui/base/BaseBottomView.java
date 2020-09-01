package org.various.player.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.various.player.PlayerConstants;
import org.various.player.core.PlayerManager;
import org.various.player.listener.UserDragSeekBarListener;
import org.various.player.utils.Repeater;
import org.various.player.utils.TimeFormatUtil;

/**
 * Created by 江雨寒 on 2020/8/19
 * Email：847145851@qq.com
 * func:
 */
public abstract class BaseBottomView extends FrameLayout implements SeekBar.OnSeekBarChangeListener {
    protected ImageView img_switch_screen;
    protected SeekBar video_seek;
    protected TextView tv_current, tv_total;
    protected UserDragSeekBarListener dragSeekBarListener;

    @NonNull
    protected Repeater progressPollRepeater = new Repeater();

    public BaseBottomView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public BaseBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BaseBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    protected abstract int setLayoutId();

    public void initView(Context context) {
        View.inflate(context, setLayoutId(), this);

    }

    public void setVisibleStatus(@PlayerConstants.VisibleStatus int status) {
        if (status == PlayerConstants.SHOW) {
            show();
            return;
        }
        if (status == PlayerConstants.HIDE) {
            hide();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        progressPollRepeater.stop();
        progressPollRepeater.setRepeatListener(null);
    }

    public abstract void show();

    public abstract void hide();

    public abstract void setOnBottomClickListener(OnClickListener listener);

    public void startRepeater() {
        progressPollRepeater.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        progressPollRepeater.setRepeatListener(new Repeater.RepeatListener() {
            @Override
            public void onRepeat() {
                updateProgress();
            }
        });
    }

    public void setCurrentTime(String str) {
        if (tv_current != null)
            tv_current.setText(str);
    }

    public void setTotalTime(String str) {
        if (tv_total != null)
            tv_total.setText(str);
    }

    public void setSeekPosition(int currentPosition, int bufferPercent) {
        if (video_seek != null) {
            video_seek.setProgress(currentPosition);
            video_seek.setSecondaryProgress(bufferPercent);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;
        long time = PlayerManager.getPlayer().getDuration() * progress / 100;
        String str = TimeFormatUtil.formatMs(time);
        setCurrentTime(str);

    }

    private void updateProgress() {
        long currentTime = PlayerManager.getPlayer().getCurrentPosition();
        String currentStr = TimeFormatUtil.formatMs(currentTime);
        setCurrentTime(currentStr);
        if (tv_total != null && "--:--".equals(tv_total.getText().toString())) {
            long totalTime = PlayerManager.getPlayer().getDuration();
            String totalStr = TimeFormatUtil.formatMs(totalTime);
            setTotalTime(totalStr);
        }
        float duration = PlayerManager.getPlayer().getDuration();
        int currentPosition = (int) ((PlayerManager.getPlayer().getCurrentPosition() / duration) * 100);
        int bufferPercent = PlayerManager.getPlayer().getBufferedPercent();
        setSeekPosition(currentPosition, bufferPercent);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        progressPollRepeater.stop();
        dragSeekBarListener.onUserDrag(UserDragSeekBarListener.DRAG_START,0);
    }



    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        long time = PlayerManager.getPlayer().getDuration() * progress / 100;
        PlayerManager.getPlayer().seekTo(time);
        progressPollRepeater.start();
        if (dragSeekBarListener != null) {
            dragSeekBarListener.onUserDrag(UserDragSeekBarListener.DRAG_END,time);
        }

    }

    public void  onScreenOrientationChanged(int currentOrientation){
        Log.e("BaseBottomView","user ScreenOrientationChanged="+currentOrientation);
    }

    public void setDragSeekListener(UserDragSeekBarListener listener) {
        this.dragSeekBarListener = listener;
    }
    public ImageView getImgSwitchScreen(){
        return img_switch_screen;
    }
}