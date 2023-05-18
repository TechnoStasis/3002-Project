#include <stdio.h>

void main() {
    int numbers[] = {1, 2, 3, 4, 5, 6};
    int count = sizeof(numbers) / sizeof(numbers[0]);
    int sum = 0;
    double average;

    for (int i = 0; i < count; i++) {
        sum += numbers[i];
    }

    average = (double) sum / count;

    printf("%.2f\n", average);

}