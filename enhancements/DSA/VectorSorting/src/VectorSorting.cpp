//============================================================================
// Name        : VectorSorting.cpp
// Author      : Shekhar Chaudhary
// Date        : 10.3.23
// Version     : 1.0
// Copyright   : Copyright © 2017 SNHU COCE
// Description : Vector Sorting Algorithms
//============================================================================

#include <algorithm>
#include <iostream>
#include <time.h>
#include <chrono>
#include <unordered_map>
#include <fstream>

#include "CSVparser.hpp" // include third party parsing library

using namespace std;

//============================================================================
// Global definitions visible to all methods and classes
//============================================================================

// forward declarations
double strToDouble(string str, char ch);

// define a structure to hold bid information
struct Bid {
    string bidId; // unique identifier
    string title;
    string fund;
    double amount;
    Bid() {
        amount = 0.0;
    }
};

//============================================================================
// Static methods used for testing
//============================================================================

/**
 * Display the bid information to the console (std::out)
 *
 * @param bid struct containing the bid info
 */
void displayBid(Bid bid) {
    cout << bid.bidId << ": " << bid.title << " | " << bid.amount << " | "
            << bid.fund << endl;
    return;
}

/**
 * Prompt user for bid information using console (std::in)
 *
 * @return Bid struct containing the bid info
 */
Bid getBid() {
    Bid bid;

    cout << "Enter Id: ";
    cin.ignore();
    getline(cin, bid.bidId);

    cout << "Enter title: ";
    getline(cin, bid.title);

    cout << "Enter fund: ";
    cin >> bid.fund;

    cout << "Enter amount: ";
    cin.ignore();
    string strAmount;
    getline(cin, strAmount);
    bid.amount = strToDouble(strAmount, '$');

    return bid;
}

/**
 * Load a CSV file containing bids into a container
 *
 * @param csvPath the path to the CSV file to load
 * @return a container holding all the bids read
 */
vector<Bid> loadBids(string csvPath) {
    cout << "Loading CSV file " << csvPath << endl;

    // Define a vector data structure to hold a collection of bids.
    vector<Bid> bids;

    // initialize the CSV Parser using the given path
    csv::Parser file = csv::Parser(csvPath);

    try {
        // loop to read rows of a CSV file
        for (int i = 0; i < file.rowCount(); i++) {

            // Create a data structure and add to the collection of bids
            Bid bid;
            bid.bidId = file[i][1];
            bid.title = file[i][0];
            bid.fund = file[i][8];
            bid.amount = strToDouble(file[i][4], '$');

            // push this bid to the end
            bids.push_back(bid);
        }
    } catch (csv::Error &e) {
        std::cerr << e.what() << std::endl;
    }
    return bids;
}

/**
 * Partition the vector of bids into two parts, low and high
 *
 * @param bids Address of the vector<Bid> instance to be partitioned
 * @param begin Beginning index to partition
 * @param end Ending index to partition
 */
int partition(vector<Bid>& bids, int start, int end) {
    int left = start;// Start of the segment
    int right = end;// End of the segment
    Bid pivotBid = bids[(start + end) / 2];  // Pivot is the middle element

    // Perform the partitioning around the pivot
    while (left <= right) { // Keep looping until they cross over

        // Move left closer to the pivot or until it finds an element that should be on the pivot's right
        while (bids[left].title < pivotBid.title) {
            left++;
        }

        // Move right closer to the pivot or until it finds an element that should be on the pivot's left
        while (bids[right].title > pivotBid.title) {
            right--;
        }

        // If elements are on the wrong side, swap them
        if (left <= right) {
            swap(bids[left], bids[right]);
            left++;
            right--;
        }
    }

    // Return the partition point where right points to the last element on the left side
    return right;
}


/**
 * Perform a quick sort on bid title
 * Average performance: O(n log(n))
 * Worst case performance O(n^2))
 *
 * @param bids address of the vector<Bid> instance to be sorted
 * @param begin the beginning index to sort on
 * @param end the ending index to sort on
 */
void quickSort(vector<Bid>& bids, int begin, int end) {
    int mid = 0;

    /* Base case: If there are 1 or zero elements to sort,
     partition is already sorted */
    if (begin >= end) {
        return;
    }

    /* Partition the data within the array. Value mid returned
     from partitioning is location of last element in low partition. */
    mid = partition(bids, begin, end);

    /* Recursively sort low partition (begin to mid) and
     high partition (begin + 1 to end) */
    quickSort(bids, begin, mid);
    quickSort(bids, mid + 1, end);

    return;
}


/**
 * Perform a selection sort on bid title
 * Average performance: O(n^2))
 * Worst case performance O(n^2))
 *
 * @param bid address of the vector<Bid>
 *            instance to be sorted
 */
void selectionSort(vector<Bid>& bids) {

    int lowestAlphaTitle = 0; // index of lowest alphabetical title bid

    for (unsigned int i = 0; i < bids.size(); ++i) {

        // Find index of smallest remaining element
        lowestAlphaTitle = i;
        for (unsigned int j = i + 1; j < bids.size(); ++j) {

            // Compare the object at j index with lowestAlphaTitle
            if (bids.at(j).title.compare(bids.at(lowestAlphaTitle).title) < 0) {
                lowestAlphaTitle = j; // if j(title) < lowestAlphaTitle, reset lowestAlphaTitle
            }
        }

        // Swap bids.at(i) and bids.at(lowestAlphaTitle)
        if (lowestAlphaTitle != i) {
            swap(bids.at(i), bids.at(lowestAlphaTitle));
        }
    }
}

/**
 * Merge two sorted subarrays into one sorted array
 * Helper function for mergeSort
 *
 * @param bids The vector containing the bids
 * @param left Starting index of the left subarray
 * @param mid Ending index of the left subarray
 * @param right Ending index of the right subarray
 */
void merge(vector<Bid>& bids, int left, int mid, int right) {
    int leftSize = mid - left + 1;
    int rightSize = right - mid;

    // Create temporary vectors
    vector<Bid> leftArray(leftSize);
    vector<Bid> rightArray(rightSize);

    // Copy data to temporary vectors
    for (int i = 0; i < leftSize; i++) {
        leftArray[i] = bids[left + i];
    }
    for (int i = 0; i < rightSize; i++) {
        rightArray[i] = bids[mid + 1 + i];
    }

    // Merge the temporary vectors back into bids
    int i = 0;      // Initial index of left subarray
    int j = 0;      // Initial index of right subarray
    int k = left;   // Initial index of merged subarray

    while (i < leftSize && j < rightSize) {
        if (leftArray[i].title <= rightArray[j].title) {
            bids[k] = leftArray[i];
            i++;
        } else {
            bids[k] = rightArray[j];
            j++;
        }
        k++;
    }

    // Copy remaining elements of leftArray, if any
    while (i < leftSize) {
        bids[k] = leftArray[i];
        i++;
        k++;
    }

    // Copy remaining elements of rightArray, if any
    while (j < rightSize) {
        bids[k] = rightArray[j];
        j++;
        k++;
    }
}

/**
 * Perform a merge sort on bid title
 * Average performance: O(n log(n))
 * Worst case performance: O(n log(n))
 * Space complexity: O(n)
 * Stable sorting algorithm
 *
 * @param bids address of the vector<Bid> instance to be sorted
 * @param left the beginning index to sort on
 * @param right the ending index to sort on
 */
void mergeSort(vector<Bid>& bids, int left, int right) {
    if (left < right) {
        // Find the middle point
        int mid = left + (right - left) / 2;

        // Sort first and second halves
        mergeSort(bids, left, mid);
        mergeSort(bids, mid + 1, right);

        // Merge the sorted halves
        merge(bids, left, mid, right);
    }
}

/**
 * Simple C function to convert a string to a double
 * after stripping out unwanted char
 *
 * credit: http://stackoverflow.com/a/24875936
 *
 * @param ch The character to strip out
 */
double strToDouble(string str, char ch) {
    str.erase(remove(str.begin(), str.end(), ch), str.end());
    return atof(str.c_str());
}

/**
 * Search for a bid by title using binary search
 * Requires the vector to be sorted first
 * Time complexity: O(log n)
 *
 * @param bids The sorted vector of bids
 * @param title The title to search for
 * @return Index of the bid if found, -1 otherwise
 */
int binarySearch(const vector<Bid>& bids, const string& title) {
    int left = 0;
    int right = bids.size() - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (bids[mid].title == title) {
            return mid;
        }

        if (bids[mid].title < title) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return -1; // Not found
}

/**
 * Export benchmark results to CSV file
 *
 * @param filename The output CSV filename
 * @param algorithm The name of the sorting algorithm
 * @param dataSize The size of the dataset
 * @param timeMs The execution time in milliseconds
 */
void exportBenchmarkToCSV(const string& filename, const string& algorithm,
                          int dataSize, double timeMs) {
    ofstream file;

    // Check if file exists to determine if we need to write header
    ifstream checkFile(filename);
    bool fileExists = checkFile.good();
    checkFile.close();

    file.open(filename, ios::app);

    if (!fileExists) {
        file << "Algorithm,DataSize,TimeMs" << endl;
    }

    file << algorithm << "," << dataSize << "," << timeMs << endl;
    file.close();

    cout << "Benchmark result exported to " << filename << endl;
}

/**
 * The one and only main() method
 */
int main(int argc, char* argv[]) {

    // process command line arguments
    string csvPath;
    switch (argc) {
    case 2:
        csvPath = argv[1];
        break;
    default:
        csvPath = "eBid_Monthly_Sales_Dec_2016.csv";
    }

    // Define a vector to hold all the bids
    vector<Bid> bids;

    // Define a HashMap for O(1) lookup
    unordered_map<string, Bid> bidHashMap;

    int choice = 0;
    while (choice != 9) {
        cout << "Menu:" << endl;
        cout << "  1. Load Bids" << endl;
        cout << "  2. Display All Bids" << endl;
        cout << "  3. Selection Sort All Bids" << endl;
        cout << "  4. Quick Sort All Bids" << endl;
        cout << "  5. Merge Sort All Bids" << endl;
        cout << "  6. Search Bid by Title (Binary Search)" << endl;
        cout << "  7. Search Bid by ID (HashMap)" << endl;
        cout << "  8. Run All Sorting Benchmarks" << endl;
        cout << "  9. Exit" << endl;
        cout << "Enter choice: ";
        cin >> choice;

        switch (choice) {

        case 1: {
            // Initialize a timer using chrono for better precision
            auto start = chrono::steady_clock::now();

            // Complete the method call to load the bids
            bids = loadBids(csvPath);

            // Populate HashMap for O(1) lookup by bidId
            bidHashMap.clear();
            for (const auto& bid : bids) {
                bidHashMap[bid.bidId] = bid;
            }

            cout << bids.size() << " bids read" << endl;

            // Calculate elapsed time and display result
            auto end = chrono::steady_clock::now();
            auto duration = chrono::duration_cast<chrono::milliseconds>(end - start);
            cout << "time: " << duration.count() << " milliseconds" << endl;

            break;
        }

        case 2:
            // Loop and display the bids read
            for (int i = 0; i < bids.size(); ++i) {
                displayBid(bids[i]);
            }
            cout << endl;

            break;

        case 3: {
            // Create a copy to preserve original data
            vector<Bid> sortedBids = bids;

            // Initialize a timer using chrono
            auto start = chrono::steady_clock::now();

            // Complete the method call to selectionSort the bids
            selectionSort(sortedBids);

            cout << sortedBids.size() << " bids sorted" << endl;

            // Calculate elapsed time and display result
            auto end = chrono::steady_clock::now();
            auto duration = chrono::duration_cast<chrono::milliseconds>(end - start);
            cout << "time: " << duration.count() << " milliseconds" << endl;

            // Export benchmark
            exportBenchmarkToCSV("benchmark_results.csv", "SelectionSort",
                                sortedBids.size(), duration.count());

            bids = sortedBids;

            break;
        }

        case 4: {
            // Create a copy to preserve original data
            vector<Bid> sortedBids = bids;

            // Initialize a timer using chrono
            auto start = chrono::steady_clock::now();

            // Complete the method call to quickSort the bids
            quickSort(sortedBids, 0, sortedBids.size() - 1);

            cout << sortedBids.size() << " bids sorted" << endl;

            // Calculate elapsed time and display result
            auto end = chrono::steady_clock::now();
            auto duration = chrono::duration_cast<chrono::milliseconds>(end - start);
            cout << "time: " << duration.count() << " milliseconds" << endl;

            // Export benchmark
            exportBenchmarkToCSV("benchmark_results.csv", "QuickSort",
                                sortedBids.size(), duration.count());

            bids = sortedBids;

            break;
        }

        case 5: {
            // Create a copy to preserve original data
            vector<Bid> sortedBids = bids;

            // Initialize a timer using chrono
            auto start = chrono::steady_clock::now();

            // Complete the method call to mergeSort the bids
            mergeSort(sortedBids, 0, sortedBids.size() - 1);

            cout << sortedBids.size() << " bids sorted" << endl;

            // Calculate elapsed time and display result
            auto end = chrono::steady_clock::now();
            auto duration = chrono::duration_cast<chrono::milliseconds>(end - start);
            cout << "time: " << duration.count() << " milliseconds" << endl;

            // Export benchmark
            exportBenchmarkToCSV("benchmark_results.csv", "MergeSort",
                                sortedBids.size(), duration.count());

            bids = sortedBids;

            break;
        }

        case 6: {
            if (bids.empty()) {
                cout << "Please load bids first." << endl;
                break;
            }

            string searchTitle;
            cout << "Enter bid title to search: ";
            cin.ignore();
            getline(cin, searchTitle);

            auto start = chrono::steady_clock::now();
            int index = binarySearch(bids, searchTitle);
            auto end = chrono::steady_clock::now();
            auto duration = chrono::duration_cast<chrono::microseconds>(end - start);

            if (index != -1) {
                cout << "Bid found:" << endl;
                displayBid(bids[index]);
            } else {
                cout << "Bid not found." << endl;
            }

            cout << "Search time: " << duration.count() << " microseconds" << endl;

            break;
        }

        case 7: {
            if (bidHashMap.empty()) {
                cout << "Please load bids first." << endl;
                break;
            }

            string searchId;
            cout << "Enter bid ID to search: ";
            cin >> searchId;

            auto start = chrono::steady_clock::now();
            auto it = bidHashMap.find(searchId);
            auto end = chrono::steady_clock::now();
            auto duration = chrono::duration_cast<chrono::microseconds>(end - start);

            if (it != bidHashMap.end()) {
                cout << "Bid found:" << endl;
                displayBid(it->second);
            } else {
                cout << "Bid not found." << endl;
            }

            cout << "Search time (HashMap O(1)): " << duration.count() << " microseconds" << endl;

            break;
        }

        case 8: {
            if (bids.empty()) {
                cout << "Please load bids first." << endl;
                break;
            }

            cout << "\n=== Running All Sorting Benchmarks ===" << endl;

            // Selection Sort Benchmark
            cout << "\n1. Selection Sort..." << endl;
            vector<Bid> tempBids = bids;
            auto start = chrono::steady_clock::now();
            selectionSort(tempBids);
            auto end = chrono::steady_clock::now();
            auto duration = chrono::duration_cast<chrono::milliseconds>(end - start);
            cout << "Time: " << duration.count() << " ms" << endl;
            exportBenchmarkToCSV("benchmark_results.csv", "SelectionSort",
                                tempBids.size(), duration.count());

            // Quick Sort Benchmark
            cout << "\n2. Quick Sort..." << endl;
            tempBids = bids;
            start = chrono::steady_clock::now();
            quickSort(tempBids, 0, tempBids.size() - 1);
            end = chrono::steady_clock::now();
            duration = chrono::duration_cast<chrono::milliseconds>(end - start);
            cout << "Time: " << duration.count() << " ms" << endl;
            exportBenchmarkToCSV("benchmark_results.csv", "QuickSort",
                                tempBids.size(), duration.count());

            // Merge Sort Benchmark
            cout << "\n3. Merge Sort..." << endl;
            tempBids = bids;
            start = chrono::steady_clock::now();
            mergeSort(tempBids, 0, tempBids.size() - 1);
            end = chrono::steady_clock::now();
            duration = chrono::duration_cast<chrono::milliseconds>(end - start);
            cout << "Time: " << duration.count() << " ms" << endl;
            exportBenchmarkToCSV("benchmark_results.csv", "MergeSort",
                                tempBids.size(), duration.count());

            cout << "\n=== Benchmark Complete ===" << endl;
            cout << "Results exported to benchmark_results.csv" << endl;

            break;
        }

        default:

            if (choice != 9) {
                cout << "Selection not recognized. Please try again." << endl;
            }

        }
    }

    cout << "Good bye." << endl;

    return 0;
}
