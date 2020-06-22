package org.dpppt.android.sdk.internal.util;

public class Triplet <U, V,W>
    {
        public final U first;   	// first field of a Pair
        public final V second;  	// second field of a Pair
        public final W third;

        // Constructs a new Pair with specified values
        public Triplet(U first, V second, W third)
        {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        // Checks specified object is "equal to" current object or not
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (o == null || getClass() != o.getClass())
                return false;

            Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;

            // call equals() method of the underlying objects
            if (!first.equals(triplet.first))
                return false;
            if (!second.equals(triplet.second))
                return false;
            return third.equals(triplet.third);
        }

        @Override
        // Computes hash code for an object to support hash tables
        public int hashCode()
        {
            // use hash codes of the underlying objects
            return 31 * first.hashCode() + second.hashCode() + third.hashCode();
        }

        @Override
        public String toString()
        {
            return "(" + first + ", " + second + ", " + third + ")";
        }

        // Factory method for creating a Typed Pair immutable instance
        public static <U, V, W> Triplet <U, V, W> of(U a, V b, W c)
        {
            // calls private constructor
            return new Triplet<>(a, b, c);
        }
    }
