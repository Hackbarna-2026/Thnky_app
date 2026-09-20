import csv
import json

# 4 static text/code challenges, 3 cases each: a genuinely correct answer, a genuine
# but wrong attempt, and an off-topic/placeholder answer that never engages with the
# question. Every "expected correct" here was manually verified against the real
# backend during this session.
CASES = [
    # index: "Explain a database index, one sentence, no jargon, no book analogy"
    ("index",
     "It's a small structure stored next to your data whose only purpose is making "
     "lookups fast instead of scanning everything.",
     True),
    ("index",
     "It makes the database bigger so it can hold more information.",
     False),
    ("index", "hello world", False),

    # claim: "40% more features headline — name the confound"
    ("claim",
     "Teams that already ship fast might have been the ones who chose to adopt the "
     "tool, so the 40% increase could reflect the teams' existing speed rather than "
     "anything the tool caused.",
     True),
    ("claim",
     "The 40% number might just be rounded up from a smaller real number, so it "
     "could actually be closer to 35%.",
     False),
    ("claim", "1 2 3 4 5 1 2 3 4 5", False),

    # dupes: "Return the first value that shows up twice, one pass, no sort/Set one-liner"
    ("dupes",
     "function firstDuplicate(nums) {\n"
     "  const seen = new Set();\n"
     "  for (const n of nums) {\n"
     "    if (seen.has(n)) return n;\n"
     "    seen.add(n);\n"
     "  }\n"
     "  return null;\n"
     "}",
     True),
    ("dupes",
     "function firstDuplicate(nums) {\n"
     "  for (let i = 0; i < nums.length; i++) {\n"
     "    if (nums[i] === nums[nums.length - 1]) return nums[i];\n"
     "  }\n"
     "  return null;\n"
     "}",
     False),
    ("dupes", "function foo() {\n  console.log('hello world');\n}", False),

    # reverse: "Two pointers, no built-in reverse, no copy"
    ("reverse",
     "function reverseInPlace(arr) {\n"
     "  let left = 0, right = arr.length - 1;\n"
     "  while (left < right) {\n"
     "    [arr[left], arr[right]] = [arr[right], arr[left]];\n"
     "    left++;\n"
     "    right--;\n"
     "  }\n"
     "  return arr;\n"
     "}",
     True),
    ("reverse", "function reverseInPlace(arr) {\n  return arr.reverse();\n}", False),
    ("reverse", "hello world", False),
]

with open("dataset.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(["input", "expected_output"])
    for challenge_id, answer, expected_correct in CASES:
        input_json = json.dumps({"challengeId": challenge_id, "answer": answer})
        expected_json = json.dumps({"correct": expected_correct})
        writer.writerow([input_json, expected_json])

print(f"Wrote {len(CASES)} test cases to dataset.csv")
