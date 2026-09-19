# Technical reasoning - AI-generated draft for review

These explanations describe the implementation; they do not attest to the student's independent understanding.

## What invariants define a valid ResizableTaskSequence?

The size stays between zero and the positive backing-array capacity, inclusive. Every occupied slot from zero through size minus one contains a non-null value in sequence order, and every unused slot contains null. The generic contract restricts inserted values to the sequence element type.

## Why must validation occur before the structure is modified?

Validation before mutation ensures a rejected operation leaves size, order, and stored values unchanged. Add checks for null before resizing, and remove retrieves the indexed value with bounds checking before shifting elements. This prevents failed operations from leaving a partially applied change.

## Why is task ID used for equality and natural ordering?

The task ID represents logical identity independently of title and priority. Using it for equals, hashCode, and compareTo ensures equal tasks have matching hash codes and compare as zero. Natural ordering is case-sensitive lexicographic ID order.

## Why is priority implemented with a comparator rather than compareTo?

The natural order already expresses ID order and must stay consistent with ID-based equality. A separate comparator offers descending priority without changing that contract. Ascending ID breaks priority ties, although equal-by-ID tasks with different priorities can compare differently under this alternate rule.

## Which test provides the strongest evidence that resizing is correct, and why?

The test named repeated resizing doubles capacity and preserves every reference starts with capacity one and inserts 65 distinct task objects across multiple growth boundaries. After every addition it checks size, empty state, exact doubled capacity, every stored reference in order, and null unused slots. This detects loss, duplication, reordering, incorrect growth, and stale unused values across repeated resizes.
