package com.mrikso.codeeditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.mrikso.codeeditor.common.OnCaretScrollListener;
import com.mrikso.codeeditor.util.Pair;

public class YoyoNavigationMethod extends TouchNavigationMethod implements OnCaretScrollListener {

    private final Yoyo mYoyoCaret;
	private final Yoyo mYoyoStart;
	private final Yoyo mYoyoEnd;

	private boolean isStartHandleTouched = false;
	private boolean isEndHandleTouched = false;
	private boolean isCaretHandleTouched = false;
	private boolean isShowYoyoCaret = true;
	private int mYoyoSize = 0;

	public YoyoNavigationMethod(FreeScrollingTextField textField) {
		super(textField);
    
		DisplayMetrics dm = textField.getContext().getResources().getDisplayMetrics();
		mYoyoSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm);
        
		mYoyoCaret = new Yoyo(textField.getContext());
		mYoyoStart = new Yoyo(textField.getContext());
		mYoyoEnd = new Yoyo(textField.getContext());
        mTextField.setCaretListener(this);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		super.onDown(e);
		if (!isCaretTouched) {
			int x = (int) e.getX() + mTextField.getScrollX();
			int y = (int) e.getY() + mTextField.getScrollY();
			isCaretHandleTouched = mYoyoCaret.isInHandle(x, y);
			isStartHandleTouched = mYoyoStart.isInHandle(x, y);
			isEndHandleTouched = mYoyoEnd.isInHandle(x, y);

			if (isCaretHandleTouched) {
				isShowYoyoCaret = true;
				mYoyoCaret.setInitialTouch(x, y);
				mYoyoCaret.invalidateHandle();
			} else if (isStartHandleTouched) {
				mYoyoStart.setInitialTouch(x, y);
				mTextField.focusSelectionStart();
				mYoyoStart.invalidateHandle();
			} else if (isEndHandleTouched) {
				mYoyoEnd.setInitialTouch(x, y);
				mTextField.focusSelectionEnd();
				mYoyoEnd.invalidateHandle();
			}
		}
		return true;
	}

	@Override
	public boolean onUp(MotionEvent e) {
		isCaretHandleTouched = false;
		isStartHandleTouched = false;
		isEndHandleTouched = false;
		mYoyoCaret.clearInitialTouch();
		mYoyoStart.clearInitialTouch();
		mYoyoEnd.clearInitialTouch();
		super.onUp(e);
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		
        if (isCaretHandleTouched) {
			//TODO find out if ACTION_UP events are actually passed to onScroll
            mTextField.stopBlink();
            yoyoRemoveCallback();
           
			if ((e2.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                yoyoDealyedHide();
                onUp(e2);
			} else {	
				isShowYoyoCaret = true;
				moveHandle(mYoyoCaret, e2);
                mTextField.startBlink();                
			}

			return true;
		} else if (isStartHandleTouched) {
			//TODO find out if ACTION_UP events are actually passed to onScroll
			if ((e2.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
				onUp(e2);
			} else {
				moveHandle(mYoyoStart, e2);
			}

			return true;
		} else if (isEndHandleTouched) {
			//TODO find out if ACTION_UP events are actually passed to onScroll
			if ((e2.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
				onUp(e2);
			} else {
				moveHandle(mYoyoEnd, e2);
			}

			return true;
		} else {
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

    // move yoyo
	private void moveHandle(Yoyo yoyo, MotionEvent e) {
        boolean scrolled = false;
        int x = (int) e.getX() - mTextField.getPaddingLeft();
        int y = (int) e.getY() -mTextField.getPaddingTop();
        
        // If the edges of the textField content area are touched, scroll in the
        // corresponding direction.
        if (x <= mTextField.SCROLL_EDGE_SLOP / 3) {
            scrolled = mTextField.autoScrollCaret(FreeScrollingTextField.SCROLL_LEFT);
        } else if (x >= (mTextField.getContentWidth() - mTextField.SCROLL_EDGE_SLOP / 3)) {
            scrolled = mTextField.autoScrollCaret(FreeScrollingTextField.SCROLL_RIGHT);
        } else if (y < mTextField.SCROLL_EDGE_SLOP) {
            scrolled = mTextField.autoScrollCaret(FreeScrollingTextField.SCROLL_UP);
        } else if (y > (mTextField.getContentHeight() - mTextField.SCROLL_EDGE_SLOP)) {
            scrolled = mTextField.autoScrollCaret(FreeScrollingTextField.SCROLL_DOWN);
        }
       
        mTextField.setCaretScrolled(true);
        if(!scrolled) {
            mTextField.stopAutoScrollCaret();
            mTextField.setCaretScrolled(false);
            Pair foundIndex = yoyo.findNearestChar(x/*(int) e.getX()*/, y/*(int) e.getY()*/);
            int newCaretIndex = foundIndex.first;

            if (newCaretIndex >= 0) {
                mTextField.moveCaret(newCaretIndex);
                //snap the handle to the caret
                Rect newCaretBounds = mTextField.getBoundingBox(newCaretIndex);
                int newX = newCaretBounds.left + mTextField.getPaddingLeft();
                int newY = newCaretBounds.bottom + mTextField.getPaddingTop();

                yoyo.attachYoyo(newX, newY);
            }
        } 
	}
    
    // 拖yoyo滴球滚动时，保证水滴球的坐标与光标一致
    @Override
    public void updateCaret(int caretIndex) {
        if (caretIndex >= 0 && isCaretHandleTouched) {
            mTextField.moveCaret(caretIndex);
            //snap the handle to the caret
            Rect newCaretBounds = mTextField.getBoundingBox(caretIndex);
            int newX = newCaretBounds.left + mTextField.getPaddingLeft();
            int newY = newCaretBounds.bottom + mTextField.getPaddingTop();

            mYoyoCaret.attachYoyo(newX, newY);
        }
    }

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		
        int x = (int) e.getX() + mTextField.getScrollX();
		int y = (int) e.getY() + mTextField.getScrollY();
        
        yoyoRemoveCallback();
        yoyoDealyedHide();
        mTextField.setCursorVisiable(true);
		
        //ignore taps on handle
		if (mYoyoCaret.isInHandle(x, y) || mYoyoStart.isInHandle(x, y) || mYoyoEnd.isInHandle(x, y)) {
			return true;
		} else {
            mTextField.stopBlink();
            mTextField.startBlink();
			isShowYoyoCaret = true;
			return super.onSingleTapUp(e);
		}
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
        
        int x = (int) e.getX() + mTextField.getScrollX();
		int y = (int) e.getY() + mTextField.getScrollY();
        
        mTextField.stopBlink();
        mTextField.setCursorVisiable(false);
        yoyoRemoveCallback();
        
        // 如果之前有选择文本，则再次双击或者长按选择文本的区域，直接返回
        // 反之则会导致水滴错位
        if(mTextField.isSelectText()) {
            int strictCharOffset = mTextField.coordToCharIndexStrict(x, y);
            if (mTextField.inSelectionRange(strictCharOffset) ||
                isNearChar(x, y, mTextField.getSelectionStart()) ||
                isNearChar(x, y, mTextField.getSelectionEnd())) {
                // do nothing
                return true;
            }
        }
        
		//ignore taps on handle
		if (mYoyoCaret.isInHandle(x, y)) {
			mTextField.selectText(true);
			return true;
		} else if (mYoyoStart.isInHandle(x, y)) {
			return true;
		} else if(mYoyoEnd.isInHandle(x, y)){
            return true;
        }
		return super.onDoubleTap(e);
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO: Implement this method
		onDoubleTap(e);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (isCaretHandleTouched || isStartHandleTouched || isEndHandleTouched) {
			onUp(e2);
			return true;
		} else {
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}

    private Runnable yoyoAnimation = () -> {
		isShowYoyoCaret = false;
		//mTextField.yoyoRemoveCallbacks(this);
		//mTextField.invalidate();
	};

    public void yoyoDealyedHide(){
        mTextField.postDelayed(yoyoAnimation, 3000);
    }
    
    public void yoyoRemoveCallback(){
        mTextField.removeCallbacks(yoyoAnimation);
    }
    
    
	@Override
	public void onTextDrawComplete(Canvas canvas) {		
        if (!mTextField.isSelectText2()) {
			mYoyoCaret.show();
			mYoyoStart.hide();
			mYoyoEnd.hide();

			if (!isCaretHandleTouched) {
				Rect caret = mTextField.getBoundingBox(mTextField.getCaretPosition());
				int x = caret.left + mTextField.getPaddingLeft();
				int y = caret.bottom + mTextField.getPaddingTop();
				mYoyoCaret.setRestingCoord(x, y);
			}
            
            // 判断文本是否有改变，在输入或删除文本时，水滴不显
            if(mTextField.getTextChanged()){
                isShowYoyoCaret = false;
                mTextField.setTextChanged(false);
                yoyoRemoveCallback();
            } else {
			    if (isShowYoyoCaret){
				    mYoyoCaret.draw(canvas, isCaretHandleTouched);
                    yoyoDealyedHide();
                }
            }
            
		} else {
			mYoyoCaret.hide();
			mYoyoStart.show();
			mYoyoEnd.show();
           
			if (!(isStartHandleTouched && isEndHandleTouched)) {
				Rect caret = mTextField.getBoundingBox(mTextField.getSelectionStart());
				int x = caret.left + mTextField.getPaddingLeft();
				int y = caret.bottom + mTextField.getPaddingTop();
				mYoyoStart.setRestingCoord(x, y);

				Rect caret2 = mTextField.getBoundingBox(mTextField.getSelectionEnd());
				int x2 = caret2.left + mTextField.getPaddingLeft();
				int y2 = caret2.bottom + mTextField.getPaddingTop();
				mYoyoEnd.setRestingCoord(x2, y2);
			}

			mYoyoStart.drawLeft(canvas, isStartHandleTouched);
			mYoyoEnd.drawRight(canvas, isEndHandleTouched);
		}
	}

	@Override
	public Rect getCaretBloat() {
		return mYoyoCaret.HANDLE_BLOAT;
	}

	@Override
	public void onColorSchemeChanged(ColorScheme colorScheme) {
		mYoyoCaret.setHandleColor(colorScheme.getColor(ColorScheme.Colorable.CARET_BACKGROUND));
	}

	private class Yoyo {
		private final int YOYO_STRING_RESTING_HEIGHT = mYoyoSize / 3;
		private final Rect HANDLE_RECT = new Rect(0, 0, mYoyoSize, mYoyoSize) ;
		public final Rect HANDLE_BLOAT;

        private Context context;
		//coordinates where the top of the yoyo string is attached
		private int anchorX = 0;
		private int anchorY = 0;

		//coordinates of the top-left corner of the yoyo handle
		private int handleX = 0;
		private int handleY = 0;

		//the offset where the handle is first touched,
		//(0,0) being the top-left of the handle
		private int xOffset = 0;
		private int yOffset = 0;

//		private final static int YOYO_HANDLE_ALPHA = 180;
//		private final static int YOYO_HANDLE_COLOR = 0xFF0000FF;
		private final Paint yoyoPaint;

		private boolean isYoyoShow = false;

		public Yoyo(Context context) {
            this.context = context;
			int radius = getRadius();
			HANDLE_BLOAT = new Rect(
				radius,
				0,
				0,
				HANDLE_RECT.bottom + YOYO_STRING_RESTING_HEIGHT);

			yoyoPaint = new Paint();
			yoyoPaint.setColor(mTextField.getColorScheme().getColor(ColorScheme.Colorable.CARET_BACKGROUND));
			yoyoPaint.setAntiAlias(true);

		}

		public void setHandleColor(int color) {
			// TODO: Implement this method
			yoyoPaint.setColor(color);
		}

		/**
		 * Draws the yoyo handle and string. The Yoyo handle can extend into 
		 * the padding region.
		 * 
		 * @param canvas
		 * @param activated True if the yoyo is activated. This causes a 
		 * 		different image to be loaded.
		 */
		public void draw(Canvas canvas, boolean activated) {
			int radius = getRadius();
			//canvas.drawLine(anchorX, anchorY, handleX + radius, handleY + radius, yoyoPaint);
			canvas.drawArc(new RectF((int)(anchorX - radius * 1.5 + mTextField.mCursorWidth / 2) ,anchorY - radius/*- YOYO_STRING_RESTING_HEIGHT*/,
                                     (int)(handleX + radius * 2.5), handleY + radius / 2), 60, 60, true, yoyoPaint);
            canvas.drawOval(new RectF(handleX, handleY, handleX + HANDLE_RECT.right, handleY + HANDLE_RECT.bottom), yoyoPaint);
		}

        
        public void drawLeft(Canvas canvas, boolean activated) {
            canvas.rotate(45, anchorX, anchorY);
            draw(canvas, isStartHandleTouched);
            canvas.rotate(-45, anchorX, anchorY);
        }

        public void drawRight(Canvas canvas, boolean activated) {
            canvas.rotate(-45, anchorX, anchorY);
            draw(canvas, isEndHandleTouched);
            canvas.rotate(45, anchorX, anchorY);
        }

		public final int getRadius() {
			return HANDLE_RECT.right / 2;
		}

		/**
		 * Clear the yoyo at the current position and attaches it to (x, y),
		 * with the handle hanging directly below.
		 */
		public void attachYoyo(int x, int y) {
			invalidateYoyo(); //clear old position
			setRestingCoord(x, y);
			invalidateYoyo(); //update new position
		}


		/**
		 * Sets the yoyo string to be attached at (x, y), with the handle 
		 * hanging directly below, but does not trigger any redrawing
		 */
		public void setRestingCoord(int x, int y) {
			anchorX = x;
			anchorY = y;
			handleX = x - getRadius();
			handleY = y + YOYO_STRING_RESTING_HEIGHT;
		}

		private void invalidateYoyo() {
			int handleCenter = handleX + getRadius();
			int x0, x1, y0, y1;
			if (handleCenter >= anchorX) {
				x0 = anchorX;
				x1 = handleCenter + 1;
			} else {
				x0 = handleCenter;
				x1 = anchorX + 1;
			}

			if (handleY >= anchorY) {
				y0 = anchorY;
				y1 = handleY;
			} else {
				y0 = handleY;
				y1 = anchorY;
			}

			//invalidate the string area
			mTextField.invalidate(x0, y0, x1, y1);
			invalidateHandle();
		}

		public void invalidateHandle() {
			Rect handleExtent = new Rect(handleX, handleY,
										 handleX + HANDLE_RECT.right, handleY + HANDLE_RECT.bottom);
			mTextField.invalidate(handleExtent);
		}

		/**
		 * This method projects a yoyo string directly above the handle and
		 * determines which character it should be attached to, or -1 if no
		 * suitable character can be found.
		 * 
		 * (handleX, handleY) is the handle origin in screen coordinates,
		 * where (0, 0) is the top left corner of the textField, regardless of
		 * its internal scroll values.
		 * 
		 * @return Pair.first contains the nearest character while Pair.second
		 * 			is the exact character found by a strict search 
		 * 
		 */
		public Pair findNearestChar(int handleX, int handleY) {
			int attachedLeft = screenToViewX(handleX) - xOffset + getRadius();
			int attachedBottom = screenToViewY(handleY) - yOffset - YOYO_STRING_RESTING_HEIGHT - 2;

			return new Pair(mTextField.coordToCharIndex(attachedLeft, attachedBottom),
							mTextField.coordToCharIndexStrict(attachedLeft, attachedBottom));
		}

		/**
		 * Records the coordinates of the initial down event on the
		 * handle so that subsequent movement events will result in the
		 * handle being offset correctly.
		 * 
		 * Does not check if isInside(x, y). Calling methods have
		 * to ensure that (x, y) is within the handle area.
		 */
		public void setInitialTouch(int x, int y) {
			xOffset = x - handleX;
			yOffset = y - handleY;
		}

		public void clearInitialTouch() {
			xOffset = 0;
			yOffset = 0;
		}

		public boolean isShow() {
			return isYoyoShow;
		}

		public void show() {
			isYoyoShow = true;
		}

		public void hide() {
			isYoyoShow = false;
		}

		public boolean isInHandle(int x, int y) {
            return isYoyoShow && (x >= handleX - mYoyoSize
                && x < (handleX + HANDLE_RECT.right) + mYoyoSize
                && y >= handleY - HANDLE_RECT.top
                && y < (handleY + mYoyoSize + HANDLE_RECT.bottom)
                );
        }
	}//end inner class
}
