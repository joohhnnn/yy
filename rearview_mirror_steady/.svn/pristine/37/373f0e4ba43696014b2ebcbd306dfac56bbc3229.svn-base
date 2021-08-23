package com.txznet.txz.util.focus_supporter.focusfinder;

import android.graphics.Rect;
import android.view.View;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.wrappers.IFocusWrapper;

/**
 * “多维” 的FocusFinder
 * 主要逻辑拷贝自android.view.FocusFinder
 * <p>
 * 提供上下左右方向上的焦点查找
 * 在寻找指定方向的下一个焦点时，优先检查View是否有设置nextXXXFocusId
 * Created by J on 2017/4/26.
 */

public class RelativeFocusFinder extends AbsFocusFinder {
    @Override
    public Object findNextFocus(Object currentFocus, Object[] focusList, int op) {
        // find next focus in specified direction
        return findFocusInAbsoluteDirection(currentFocus, focusList, op);
    }

    private Object findFocusInAbsoluteDirection(Object currentFocus, Object[] focusList, int op) {
        Object closest = null;

        // initialize focus rect for current focus
        Rect sourceRect = getRectForSpecifiedObject(currentFocus);
        if (null == sourceRect) {
            return null;
        }

        Rect mBestCandidateRect = new Rect(sourceRect);
        int direction = NavOpToDirection(op);

        // initialize the best candidate to something impossible
        // (so the first plausible view will become the best choice)
        switch (direction) {
            case View.FOCUS_LEFT:
                mBestCandidateRect.offset(sourceRect.width() + 1, 0);
                break;
            case View.FOCUS_RIGHT:
                mBestCandidateRect.offset(-(sourceRect.width() + 1), 0);
                break;
            case View.FOCUS_UP:
                mBestCandidateRect.offset(0, sourceRect.height() + 1);
                break;
            case View.FOCUS_DOWN:
                mBestCandidateRect.offset(0, -(sourceRect.height() + 1));
        }

        for (int i = 0, len = focusList.length; i < len; i++) {
            Object obj = focusList[i];

            // only interested in other views
            if (obj == currentFocus) continue;

            // get focus bounds of other view in same coordinate system
            Rect rect = getRectForSpecifiedObject(obj);

            if (isBettrerCandidate(NavOpToDirection(op), sourceRect, rect, mBestCandidateRect)) {
                mBestCandidateRect.set(rect);
                closest = obj;
            }
        }
        return closest;
    }

    private int NavOpToDirection(int op) {
        if (FocusSupporter.NAV_BTN_LEFT == op) {
            return View.FOCUS_LEFT;
        }

        if (FocusSupporter.NAV_BTN_RIGHT == op) {
            return View.FOCUS_RIGHT;
        }

        if (FocusSupporter.NAV_BTN_UP == op) {
            return View.FOCUS_UP;
        }

        if (FocusSupporter.NAV_BTN_DOWN == op) {
            return View.FOCUS_DOWN;
        }

        return 0;
    }

    private Rect getRectForSpecifiedObject(Object object) {
        View v = null;

        if (object instanceof View) {
            v = (View) object;
        }

        if (object instanceof IFocusWrapper) {
            v = ((IFocusWrapper) object).getContent();
        }

        if (null == v) {
            return null;
        }

        int[] arrLocation = new int[2];

        v.getLocationOnScreen(arrLocation);
        return new Rect(arrLocation[0], arrLocation[1], arrLocation[0] + v.getWidth(), arrLocation[1] + v.getHeight());
    }

    /**
     * Is rect1 a better candidate than rect2 for a focus search in a particular
     * direction from a source rect?  This is the core routine that determines
     * the order of focus searching.
     *
     * @param direction the direction (up, down, left, right)
     * @param source    The source we are searching from
     * @param rect1     The candidate rectangle
     * @param rect2     The current best candidate.
     * @return Whether the candidate is the new best.
     */
    private boolean isBettrerCandidate(int direction, Rect source, Rect rect1, Rect rect2) {
        if (null == rect1) {
            return false;
        }

        if (null == rect2) {
            return true;
        }

        // to be a better candidate, need to at least be a candidate in the first
        // place :)
        if (!isCandidate(source, rect1, direction)) {
            return false;
        }

        // we know that rect1 is a candidate.. if rect2 is not a candidate,
        // rect1 is better
        if (!isCandidate(source, rect2, direction)) {
            return true;
        }

        // if rect1 is better by beam, it wins
        if (beamBeats(direction, source, rect1, rect2)) {
            return true;
        }

        // if rect2 is better, then rect1 cant' be :)
        if (beamBeats(direction, source, rect2, rect1)) {
            return false;
        }

        // otherwise, do fudge-tastic comparison of the major and minor axis
        return (getWeightedDistanceFor(
                majorAxisDistance(direction, source, rect1),
                minorAxisDistance(direction, source, rect1))
                < getWeightedDistanceFor(
                majorAxisDistance(direction, source, rect2),
                minorAxisDistance(direction, source, rect2)));
    }

    boolean isCandidate(Rect srcRect, Rect destRect, int op) {
        switch (op) {
            case View.FOCUS_LEFT:
                return (srcRect.right > destRect.right || srcRect.left >= destRect.right)
                        && srcRect.left > destRect.left;
            case View.FOCUS_RIGHT:
                return (srcRect.left < destRect.left || srcRect.right <= destRect.left)
                        && srcRect.right < destRect.right;
            case View.FOCUS_UP:
                return (srcRect.bottom > destRect.bottom || srcRect.top >= destRect.bottom)
                        && srcRect.top > destRect.top;
            case View.FOCUS_DOWN:
                return (srcRect.top < destRect.top || srcRect.bottom <= destRect.top)
                        && srcRect.bottom < destRect.bottom;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * One rectangle may be another candidate than another by virtue of being
     * exclusively in the beam of the source rect.
     *
     * @return Whether rect1 is a better candidate than rect2 by virtue of it being in src's
     * beam
     */
    boolean beamBeats(int direction, Rect source, Rect rect1, Rect rect2) {
        final boolean rect1InSrcBeam = beamsOverlap(direction, source, rect1);
        final boolean rect2InSrcBeam = beamsOverlap(direction, source, rect2);

        // if rect1 isn't exclusively in the src beam, it doesn't win
        if (rect2InSrcBeam || !rect1InSrcBeam) {
            return false;
        }

        // we know rect1 is in the beam, and rect2 is not

        // if rect1 is to the direction of, and rect2 is not, rect1 wins.
        // for example, for direction left, if rect1 is to the left of the source
        // and rect2 is below, then we always prefer the in beam rect1, since rect2
        // could be reached by going down.
        if (!isToDirectionOf(direction, source, rect2)) {
            return true;
        }

        // for horizontal directions, being exclusively in beam always wins
        if ((direction == View.FOCUS_LEFT || direction == View.FOCUS_RIGHT)) {
            return true;
        }

        // for vertical directions, beams only beat up to a point:
        // now, as long as rect2 isn't completely closer, rect1 wins
        // e.g for direction down, completely closer means for rect2's top
        // edge to be closer to the source's top edge than rect1's bottom edge.
        return (majorAxisDistance(direction, source, rect1)
                < majorAxisDistanceToFarEdge(direction, source, rect2));
    }

    /**
     * Do the "beams" w.r.t the given direction's axis of rect1 and rect2 overlap?
     *
     * @param direction the direction (up, down, left, right)
     * @param rect1     The first rectangle
     * @param rect2     The second rectangle
     * @return whether the beams overlap
     */
    boolean beamsOverlap(int direction, Rect rect1, Rect rect2) {
        switch (direction) {
            case View.FOCUS_LEFT:
            case View.FOCUS_RIGHT:
                return (rect2.bottom >= rect1.top) && (rect2.top <= rect1.bottom);
            case View.FOCUS_UP:
            case View.FOCUS_DOWN:
                return (rect2.right >= rect1.left) && (rect2.left <= rect1.right);
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * e.g for left, is 'to left of'
     */
    static boolean isToDirectionOf(int direction, Rect src, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
                return src.left >= dest.right;
            case View.FOCUS_RIGHT:
                return src.right <= dest.left;
            case View.FOCUS_UP:
                return src.top >= dest.bottom;
            case View.FOCUS_DOWN:
                return src.bottom <= dest.top;
        }

        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * @return The distance from the edge furthest in the given direction
     * of source to the edge nearest in the given direction of dest.  If the
     * dest is not in the direction from source, return 0.
     */
    static int majorAxisDistance(int direction, Rect source, Rect dest) {
        return Math.max(0, majorAxisDistanceRaw(direction, source, dest));
    }

    static int majorAxisDistanceRaw(int direction, Rect source, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
                return source.left - dest.right;
            case View.FOCUS_RIGHT:
                return dest.left - source.right;
            case View.FOCUS_UP:
                return source.top - dest.bottom;
            case View.FOCUS_DOWN:
                return dest.top - source.bottom;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * @return The distance along the major axis w.r.t the direction from the
     * edge of source to the far edge of dest. If the
     * dest is not in the direction from source, return 1 (to break ties with
     * {@link #majorAxisDistance}).
     */
    static int majorAxisDistanceToFarEdge(int direction, Rect source, Rect dest) {
        return Math.max(1, majorAxisDistanceToFarEdgeRaw(direction, source, dest));
    }

    static int majorAxisDistanceToFarEdgeRaw(int direction, Rect source, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
                return source.left - dest.left;
            case View.FOCUS_RIGHT:
                return dest.right - source.right;
            case View.FOCUS_UP:
                return source.top - dest.top;
            case View.FOCUS_DOWN:
                return dest.bottom - source.bottom;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * Find the distance on the minor axis w.r.t the direction to the nearest
     * edge of the destination rectangle.
     *
     * @param direction the direction (up, down, left, right)
     * @param source    The source rect.
     * @param dest      The destination rect.
     * @return The distance.
     */
    static int minorAxisDistance(int direction, Rect source, Rect dest) {
        switch (direction) {
            case View.FOCUS_LEFT:
            case View.FOCUS_RIGHT:
                // the distance between the center verticals
                return Math.abs(
                        ((source.top + source.height() / 2) -
                                ((dest.top + dest.height() / 2))));
            case View.FOCUS_UP:
            case View.FOCUS_DOWN:
                // the distance between the center horizontals
                return Math.abs(
                        ((source.left + source.width() / 2) -
                                ((dest.left + dest.width() / 2))));
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }

    /**
     * Fudge-factor opportunity: how to calculate distance given major and minor
     * axis distances.  Warning: this fudge factor is finely tuned, be sure to
     * run all focus tests if you dare tweak it.
     */
    static int getWeightedDistanceFor(int majorAxisDistance, int minorAxisDistance) {
        return 13 * majorAxisDistance * majorAxisDistance
                + minorAxisDistance * minorAxisDistance;
    }
}
