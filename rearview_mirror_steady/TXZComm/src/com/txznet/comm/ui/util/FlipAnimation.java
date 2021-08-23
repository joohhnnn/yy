package com.txznet.comm.ui.util;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class FlipAnimation extends Animation {
	private float mFromDegrees;
	private float mToDegrees;

	private int mPivotXType = ABSOLUTE;
	private int mPivotYType = ABSOLUTE;
	private float mPivotXValue = 0.0f;
	private float mPivotYValue = 0.0f;

	private float mPivotX;
	private float mPivotY;

	/**
	 * Constructor to use when building a RotateAnimation from code. Default
	 * pivotX/pivotY point is (0,0).
	 * 
	 * @param fromDegrees
	 *            Rotation offset to apply at the start of the animation.
	 * 
	 * @param toDegrees
	 *            Rotation offset to apply at the end of the animation.
	 */
	public FlipAnimation(float fromDegrees, float toDegrees) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mPivotX = 0.0f;
		mPivotY = 0.0f;
	}

	/**
	 * Constructor to use when building a RotateAnimation from code
	 * 
	 * @param fromDegrees
	 *            Rotation offset to apply at the start of the animation.
	 * 
	 * @param toDegrees
	 *            Rotation offset to apply at the end of the animation.
	 * 
	 * @param pivotX
	 *            The X coordinate of the point about which the object is being
	 *            rotated, specified as an absolute number where 0 is the left
	 *            edge.
	 * @param pivotY
	 *            The Y coordinate of the point about which the object is being
	 *            rotated, specified as an absolute number where 0 is the top
	 *            edge.
	 */
	public FlipAnimation(float fromDegrees, float toDegrees, float pivotX,
			float pivotY) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;

		mPivotXType = ABSOLUTE;
		mPivotYType = ABSOLUTE;
		mPivotXValue = pivotX;
		mPivotYValue = pivotY;
		initializePivotPoint();
	}

	/**
	 * Constructor to use when building a RotateAnimation from code
	 * 
	 * @param fromDegrees
	 *            Rotation offset to apply at the start of the animation.
	 * 
	 * @param toDegrees
	 *            Rotation offset to apply at the end of the animation.
	 * 
	 * @param pivotXType
	 *            Specifies how pivotXValue should be interpreted. One of
	 *            Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
	 *            Animation.RELATIVE_TO_PARENT.
	 * @param pivotXValue
	 *            The X coordinate of the point about which the object is being
	 *            rotated, specified as an absolute number where 0 is the left
	 *            edge. This value can either be an absolute number if
	 *            pivotXType is ABSOLUTE, or a percentage (where 1.0 is 100%)
	 *            otherwise.
	 * @param pivotYType
	 *            Specifies how pivotYValue should be interpreted. One of
	 *            Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
	 *            Animation.RELATIVE_TO_PARENT.
	 * @param pivotYValue
	 *            The Y coordinate of the point about which the object is being
	 *            rotated, specified as an absolute number where 0 is the top
	 *            edge. This value can either be an absolute number if
	 *            pivotYType is ABSOLUTE, or a percentage (where 1.0 is 100%)
	 *            otherwise.
	 */
	public FlipAnimation(float fromDegrees, float toDegrees, int pivotXType,
			float pivotXValue, int pivotYType, float pivotYValue) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;

		mPivotXValue = pivotXValue;
		mPivotXType = pivotXType;
		mPivotYValue = pivotYValue;
		mPivotYType = pivotYType;
		initializePivotPoint();
	}

	/**
	 * Called at the end of constructor methods to initialize, if possible,
	 * values for the pivot point. This is only possible for ABSOLUTE pivot
	 * values.
	 */
	private void initializePivotPoint() {
		if (mPivotXType == ABSOLUTE) {
			mPivotX = mPivotXValue;
		}
		if (mPivotYType == ABSOLUTE) {
			mPivotY = mPivotYValue;
		}
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float degrees = mFromDegrees
				+ ((mToDegrees - mFromDegrees) * interpolatedTime);

		Matrix matrix = t.getMatrix();
		Camera camera = new Camera();
		camera.save();

		// 设置camera动作为绕X轴旋转
		camera.rotateX(degrees);

		// 根据camera动作产生一个matrix，赋给Transformation的matrix，以用来设置动画效果
		camera.getMatrix(matrix);
		matrix.preTranslate(-mPivotX , -mPivotY );
		matrix.postTranslate(mPivotX , mPivotY );

		// if (mPivotX == 0.0f && mPivotY == 0.0f) {
		// t.getMatrix().setRotate(degrees);
		// } else {
		// t.getMatrix().setRotate(degrees, mPivotX * scale, mPivotY * scale);
		// }
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
		mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
	}

}
