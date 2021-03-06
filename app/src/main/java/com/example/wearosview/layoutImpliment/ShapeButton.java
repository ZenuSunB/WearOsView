package com.example.wearosview.layoutImpliment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatButton;

import com.example.wearosview.R;

public class ShapeButton extends AppCompatButton {
    private int normal_color;
    private int pressed_color;
    private int enabled_color;
    private int gravity;
    private int radius_size;


    public ShapeButton(Context context) {
        this(context, null);
    }

    public ShapeButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ShapeButton);
        normal_color = ta.getColor(R.styleable.ShapeButton_normal_color, Color.parseColor("#FF3333"));
        pressed_color = ta.getColor(R.styleable.ShapeButton_pressed_color, Color.parseColor("#CC3333"));
        enabled_color = ta.getColor(R.styleable.ShapeButton_enabled_color, Color.GRAY);
        radius_size = (int) ta.getDimension(R.styleable.ShapeButton_radius_size, dip2px(4));
        gravity = ta.getInt(R.styleable.ShapeButton_android_gravity, Gravity.CENTER);
//        int textColor = attrs.getAttributeIntValue(
//                "http://schemas.android.com/apk/res/android", "textColor", Color.WHITE);
//        setTextColor(textColor);
        ta.recycle();
        TypedArray tar = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.textColor, android.R.attr.paddingTop, android.R.attr.paddingBottom});
        if (tar != null) {
            setTextColor(tar.getColor(0, Color.WHITE));
            setPadding(6, (int) tar.getDimension(1, 8), 6, (int) tar.getDimension(2, 8));
        }
        setGravity(gravity);
        tar.recycle();
        init();
    }

    @Override
    public void setTextColor(@ColorInt int color) {
        super.setTextColor(color);
    }

    private void init() {
        setBackgroundDrawable(getStateListDrawable(getSolidRectDrawable(radius_size, pressed_color), getSolidRectDrawable(radius_size, normal_color)));
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(dip2px(left), dip2px(top), dip2px(right), dip2px(bottom));
    }

    /**
     * ???????????????drawable, ????????????????????????????????????
     *
     * @param cornerRadius ????????????
     * @param solidColor   ????????????
     * @return ??????????????????
     */
    public static GradientDrawable getSolidRectDrawable(int cornerRadius, int solidColor) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        // ???????????????????????????
        gradientDrawable.setCornerRadius(cornerRadius);
        // ????????????????????????
        gradientDrawable.setColor(solidColor);
        // ??????????????????
        gradientDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        return gradientDrawable;
    }

    /**
     * ???????????????
     *
     * @param pressedDrawable ???????????????Drawable
     * @param normalDrawable  ???????????????Drawable
     * @return ???????????????
     */
    public StateListDrawable getStateListDrawable(Drawable pressedDrawable, Drawable normalDrawable) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled}, normalDrawable);
        //????????????????????????
        //????????????????????????
        GradientDrawable gray = getSolidRectDrawable(radius_size, enabled_color);
        stateListDrawable.addState(new int[]{}, gray);
        return stateListDrawable;
    }

    private int dip2px(float dpValue) {
        final float scale = getResources()
                .getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

