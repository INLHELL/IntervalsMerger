package model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.Range;

public class Interval {
    private final int start;
    private final int end;

    public Interval(int start, int end) {
        if(start > end) {
            this.start = end;
            this.end = start;
        } else {
            this.start = start;
            this.end = end;
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Interval mergeWith(Interval other) {
        final int min = Integer.min(start, other.getStart());
        final int max = Integer.max(end, other.getEnd());
        return new Interval(min, max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;

        if (start != interval.start) return false;
        return end == interval.end;

    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    @Override
    public String toString() {
        return "Interval[" + start + ':' + end + ']';
    }


}
