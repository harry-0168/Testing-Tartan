import pandas as pd
import matplotlib.pyplot as plt
import os
import glob

# Define rate per minute
rate_per_minute = 0.05

# Function to analyze weekly data
def analyze_weekly_data(directory):
    csv_files = glob.glob(os.path.join(directory, "report-*.csv"))

    all_data = []

    for file_path in csv_files:
        df = pd.read_csv(file_path)

        # Clean column names
        df.columns = df.columns.str.strip()

        # Convert usage to seconds
        df['Light Usage Minute'] = df['Light Usage Minute'].astype(str).str.extract('(\\d+)').astype(int)
        df['Light Usage Second'] = df['Light Usage Second'].astype(str).str.extract('(\\d+)').astype(int)
        df['Total Usage Seconds'] = df['Light Usage Minute'] * 60 + df['Light Usage Second']

        # Determine variant
        variant = 'With Cost' if 'Estimated Cost' in df.columns else 'Without Cost'
        df['Variant'] = variant

        # Extract date and house name from filename
        filename = os.path.basename(file_path)
        parts = filename.replace('.csv', '').split('-')
        date = '-'.join(parts[1:4])
        house_name = parts[-1]

        df['House Name'] = house_name
        df['Date'] = pd.to_datetime(date)

        all_data.append(df[['House Name', 'Total Usage Seconds', 'Variant', 'Date']])

    combined_data = pd.concat(all_data, ignore_index=True)

    # Boxplot visualization
    plt.figure(figsize=(8, 5))
    combined_data.boxplot(column='Total Usage Seconds', by='Variant')
    plt.title('Light Usage by Report Variant')
    plt.suptitle('')
    plt.xlabel('Variant')
    plt.ylabel('Total Usage (seconds)')
    plt.tight_layout()
    plt.show()

    # Average usage comparison
    avg_usage = combined_data.groupby('Variant')['Total Usage Seconds'].mean()
    avg_usage.plot(kind='bar', figsize=(8, 5))
    plt.title('Average Light Usage by Variant')
    plt.ylabel('Average Usage (seconds)')
    plt.tight_layout()
    plt.show()

    # Weekly trend of average usage
    weekly_avg = combined_data.groupby(['Variant', 'Date']).agg({'Total Usage Seconds': 'mean'}).reset_index()

    plt.figure(figsize=(10, 6))
    for variant in weekly_avg['Variant'].unique():
        subset = weekly_avg[weekly_avg['Variant'] == variant].sort_values('Date')
        plt.plot(subset['Date'], subset['Total Usage Seconds'], marker='o', linestyle='-', label=variant)

    plt.title('Weekly Average Light Usage by Variant')
    plt.xlabel('Date')
    plt.ylabel('Average Usage (seconds)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.show()

    # Weekly rate of change
    weekly_avg_sorted = weekly_avg.sort_values(['Variant', 'Date']).copy()
    weekly_avg_sorted['Usage Change'] = weekly_avg_sorted.groupby('Variant')['Total Usage Seconds'].diff()
    weekly_avg_sorted.dropna(subset=['Usage Change'], inplace=True)

    plt.figure(figsize=(10, 6))
    for variant in weekly_avg_sorted['Variant'].unique():
        subset = weekly_avg_sorted[weekly_avg_sorted['Variant'] == variant]
        plt.plot(subset['Date'], subset['Usage Change'], marker='o', linestyle='-', label=f"{variant}")

    plt.title('Weekly Rate of Change in Average Light Usage by Variant')
    plt.xlabel('Date')
    plt.ylabel('Change in Usage (seconds)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.show()


# Example usage:
if __name__ == '__main__':
    # put the directory of all the experiment record csv data files
    directory = 'Simulation_Result_Analysis'
    analyze_weekly_data(directory)
