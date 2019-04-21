package com.triplec.triway;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Timer;

/**
 * Adapter for ViewPager in HomeActivity
 * source: https://www.jianshu.com/p/58f356eaa6e9
 */
public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    // Duplicate the first and the last images for recyclable scrolling
    private Integer[] images = {R.drawable.album_city3,     // jump to images[3]
                                R.drawable.album_city1, R.drawable.album_city2, R.drawable.album_city3,
                                R.drawable.album_city1};    // jump to images[1]
    private String[] cities = {  "Hawaii", "San Diego", "New York", "Hawaii", "San Diego"};

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        // fill ImageView with image at given index position
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.album_main, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(images[position]);

        // add onClickListener for each image
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start route activity
                if( HomeActivity.getIsClicked() ){
                    return;
                }
                else{
                    HomeActivity.setisClickedTrue();
                }
                Intent intent = new Intent(context, RouteActivity.class);
                String city = cities[position];
                intent.putExtra("place", city);
                context.startActivity(intent);
            }
        });

        // add view to ViewPager
        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}