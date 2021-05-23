package com.example.downloadmanmager.download;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadmanmager.R;

import java.util.List;

public class DownLoadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<DownLoadModel> mData;

    public DownLoadAdapter(Context mContext, List<DownLoadModel> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    public class DownLoadViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvSize;
        private ProgressBar progressBar;
        Button btnPause;
        TextView tvFileStatus;

        public DownLoadViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSize = itemView.findViewById(R.id.tvSize);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnPause = itemView.findViewById(R.id.btnPause);
            tvFileStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_row, parent, false);
        DownLoadViewHolder downLoadViewHolder = new DownLoadViewHolder(view);
        return downLoadViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DownLoadViewHolder downLoadViewHolder = (DownLoadViewHolder) holder;
        DownLoadModel downLoadModel = mData.get(position);

        downLoadViewHolder.tvTitle.setText(downLoadModel.getTitle());
        downLoadViewHolder.tvFileStatus.setText(downLoadModel.getStatus());
        downLoadViewHolder.progressBar.setProgress(Integer.parseInt(downLoadModel.progress));
        downLoadViewHolder.tvSize.setText("DownLoad:" + downLoadModel.getFile_size());

        if (downLoadModel.isPaused) {
            downLoadViewHolder.btnPause.setText("RESUME");
        } else {
            downLoadViewHolder.btnPause.setText("PAUSE");
        }

        if (downLoadModel.getStatus().equalsIgnoreCase("RESUME")) {
            downLoadViewHolder.tvFileStatus.setText("Running中..");
        }

        downLoadViewHolder.btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downLoadModel.isPaused) {
                    //如果是暫停狀態下點下去
                    downLoadModel.setPaused(false);
                    downLoadViewHolder.btnPause.setText("PAUSE");
                    downLoadModel.setStatus("RESUME");
                    downLoadViewHolder.tvFileStatus.setText("Running");
                    if (!resumeDownload(downLoadModel)) {
                        Toast.makeText(mContext, "Fail RESUME", Toast.LENGTH_SHORT).show();
                    } else {

                    }
                    notifyItemChanged(position); //更新
                } else {
                    downLoadModel.setPaused(true);
                    downLoadViewHolder.btnPause.setText("RESUME");
                    downLoadModel.setStatus("PAUSE");
                    downLoadViewHolder.tvFileStatus.setText("PAUSE");
                    if (!pauseDownload(downLoadModel)) {
                        //如果沒有暫停Download
                        Toast.makeText(mContext, "Fail Pause", Toast.LENGTH_SHORT).show();
                    }
                    notifyItemChanged(position);
                }
            }
        });


    }

    private boolean pauseDownload(DownLoadModel downLoadModel) {
        int updatedRow = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", 1);

        //
        try {
            updatedRow = mContext.getContentResolver().update(
                    Uri.parse("content://downloads/my_downloads"),
                    contentValues,
                    "title=?",
                    new String[]{downLoadModel.getTitle()}
            );

        } catch (Exception e) {

        }
        ;
        return 0 < updatedRow; //如果0 <=0 代表更新沒成功 FALE
    }

    private boolean resumeDownload(DownLoadModel downLoadModel) {
        int updatedRow = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", 0);

        //
        try {
            updatedRow = mContext.getContentResolver().update(
                    Uri.parse("content://downloads/my_downloads"),
                    contentValues,
                    "title=?",
                    new String[]{downLoadModel.getTitle()}
            );

        } catch (Exception e) {

        }
        ;
        return 0 < updatedRow; //如果0 <=0 代表更新沒成功 FALE
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    //
    public void changeItem(long downloadId){
        int i = 0;
        for(DownLoadModel downLoadModel : mData){
            if(downLoadModel.getDownloadId() == downloadId){
                notifyItemChanged(i);
                Log.v(DownLoadctivity.TAG,"changeItem -> downloadId:" + downloadId +"/downLoadModel.getId():" + downLoadModel.getId());
            }
            i++;
        }
    }


    public boolean changeItemWithStatus(String message, long downloadId){
        boolean comp = false;
        int i = 0;
        for(DownLoadModel downLoadModel : mData){
            if(downLoadModel.getDownloadId() == downloadId){
                mData.get(i).setStatus(message);
                notifyItemChanged(i);
                comp = true;
                Log.v(DownLoadctivity.TAG,"changeItemWithStatus -> downloadId:" + downloadId +"/message:" + message);
            }
            i++;
        }
        return comp;
    }

    //設定改變itemFile位置
    public void setChangeItemFilePath(String path, long completeId){
        int i = 0;
        for(DownLoadModel downLoadModel : mData){
            if(completeId == downLoadModel.getDownloadId()){
                mData.get(i).setFile_path(path);
                notifyItemChanged(i);
                Log.v(DownLoadctivity.TAG,"setChangeItemFilePath -> path:" + path +"/completeId:" + completeId);
            }
            i++;
        }
    }
}
