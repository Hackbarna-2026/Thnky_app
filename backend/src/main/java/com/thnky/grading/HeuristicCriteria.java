package com.thnky.grading;

import java.util.List;

/** Local grading rule for one static-bank challenge — see {@code challenges/heuristic-criteria.json}. */
record HeuristicCriteria(Integer minWords, List<String> keywords) {
}
