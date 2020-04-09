package cpf.marqueeview.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cpf.marqueeview.MarqueeTextureView;
import cpf.marqueeview.MarqueeView;
import cpf.marqueeview.demo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(mainBinding.getRoot());

        mainBinding.marqueeTextureViewGone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeTextureView.setVisibility(View.GONE);
            }
        });
        mainBinding.marqueeTextureViewHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeTextureView.setVisibility(View.INVISIBLE);
            }
        });
        mainBinding.marqueeTextureViewShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeTextureView.setVisibility(View.VISIBLE);
            }
        });
        mainBinding.marqueeTextureViewStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeTextureView.start();
            }
        });
        mainBinding.marqueeTextureViewStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeTextureView.stop();
            }
        });

        mainBinding.marqueeViewGone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeView.setVisibility(View.GONE);
            }
        });
        mainBinding.marqueeViewHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeView.setVisibility(View.INVISIBLE);
            }
        });
        mainBinding.marqueeViewShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeView.setVisibility(View.VISIBLE);
            }
        });
        mainBinding.marqueeViewStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeView.start();
            }
        });
        mainBinding.marqueeViewStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.marqueeView.stop();
            }
        });

        mainBinding.marqueeView.setOffset(0.6f);
        mainBinding.marqueeView.setText(getResources().getStringArray(R.array.data));
        mainBinding.marqueeView.setOnItemClickListener(new MarqueeView.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }
        });

        mainBinding.marqueeTextureView.setOffset(0.6f);
        mainBinding.marqueeTextureView.setText(getResources().getStringArray(R.array.data));
        mainBinding.marqueeTextureView.setOnItemClickListener(new MarqueeTextureView.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
