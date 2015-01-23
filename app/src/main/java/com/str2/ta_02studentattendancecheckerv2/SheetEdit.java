package com.str2.ta_02studentattendancecheckerv2;

/**
 * Created by The Administrator on 1/8/2015.
 */

import android.util.Log;

import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;

/**
 * Using this demo, you can see how GData can read and write to individual cells
 * based on their position or send a batch of update commands in one HTTP
 * request.
 *
 * Usage: java CellDemo --username [user] --password [pass]
 */

public class SheetEdit {
    /** The message for displaying the usage parameters. */
    private static final String[] USAGE_MESSAGE = {
            "Usage: java CellDemo --username [user] --password [pass] ", ""};

    /** Welcome message, introducing the program. */
    private static final String[] WELCOME_MESSAGE = {
            "This is a demo of the cells feed!", "",
            "Using this interface, you can read/write to your spreadsheet's cells.",
            ""};

    /** Help on all available commands. */
    private static final String[] COMMAND_HELP_MESSAGE = {
            "Commands:",
            " load                              [[select a spreadsheet and worksheet]]",
            " list                              [[shows all cells]]",
            " range minRow maxRow minCol maxCol [[rectangle]]",
            " set row# col# formula             [[sets a cell]]",
            "   example: set 1 3 =R1C2+1",
            " search adam                       [[full text query]]",
            " batch                             [[batch request]]",
            " exit"};

    /** Our view of Google Spreadsheets as an authenticated Google user. */
    private SpreadsheetService service;

    /** The URL of the cells feed. */
    private URL cellFeedUrl;

    /** The URL of the lists feed. */
    private URL listFeedUrl;

    /** The output stream. */
    private PrintStream out;

    /** A factory that generates the appropriate feed URLs. */
    private FeedURLFactory factory;

    /**
     * Constructs a cell demo from the specified spreadsheet service, which is
     * used to authenticate to and access Google Spreadsheets.
     *
     * @param service the connection to the Google Spradsheets service.
     * @param outputStream a handle for stdout.
     */
    public SheetEdit(SpreadsheetService service, PrintStream outputStream) {
        this.service = service;
        this.out = outputStream;
        this.factory = FeedURLFactory.getDefault();
    }

    /**
     * Log in to Google, under the Google Spreadsheets account.
     *
     * @param username name of user to authenticate (e.g. yourname@gmail.com)
     * @param password password to use for authentication
     * @throws AuthenticationException if the service is unable to validate the
     *         username and password.
     */
    public void login(String username, String password)
            throws AuthenticationException {

        // Authenticate
        service.setUserCredentials(username, password);
    }

    public URL getListFeedUrl(){
        if(listFeedUrl != null) {
            return listFeedUrl;
        } else {
            return null;
        }
    }

    /**
     * Displays the given list of entries and prompts the user to select the index
     * of one of the entries. NOTE: The displayed index is 1-based and is
     * converted to 0-based before being returned.
     *
     * @param reader to read input from the keyboard
     * @param entries the list of entries to display
     * @param type describes the type of things the list contains
     * @return the 0-based index of the user's selection
     * @throws IOException if an I/O error occurs while getting input from user
     */
    private int getIndexFromUser(BufferedReader reader, List entries, String type)
            throws IOException {
        for (int i = 0; i < entries.size(); i++) {
            BaseEntry entry = (BaseEntry) entries.get(i);
            System.out.println("\t(" + (i + 1) + ") "
                    + entry.getTitle().getPlainText());
        }
        int index = -1;
        while (true) {
            out.print("Enter the number of the spreadsheet to load: ");
            String userInput = reader.readLine();
            try {
                index = Integer.parseInt(userInput);
                if (index < 1 || index > entries.size()) {
                    throw new NumberFormatException();
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number for your selection.");
            }
        }
        return index - 1;
    }

    /**
     * Uses the user's credentials to get a list of spreadsheets. Then asks the
     * user which spreadsheet to load. If the selected spreadsheet has multiple
     * worksheets then the user will also be prompted to select what sheet to use.
     *
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     *
     */
    public void loadWorksheet(SpreadsheetEntry spreadsheet, int worksheetIndex) throws IOException,
            ServiceException {
        // Get the worksheet to load
        List worksheets = spreadsheet.getWorksheets();
        WorksheetEntry worksheet = (WorksheetEntry) worksheets
                .get(worksheetIndex);
        cellFeedUrl = worksheet.getCellFeedUrl();
        listFeedUrl = worksheet.getListFeedUrl();
    }

    public SpreadsheetEntry loadSpreadsheet(int spreadsheetIndex) throws IOException,
            ServiceException {
        SpreadsheetFeed feed = service.getFeed(factory.getSpreadsheetsFeedUrl(),
                SpreadsheetFeed.class);
        return feed.getEntries().get(spreadsheetIndex);
    }

    public String showSheet(int index, List spreadsheets) throws IOException,
            ServiceException {
        BaseEntry entry = (BaseEntry) spreadsheets.get(index);
        return (entry.getTitle().getPlainText());
    }

    public void showRows() throws
            IOException, ServiceException {
        ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);

        // Iterate through each row, printing its cell values.
        for (ListEntry row : listFeed.getEntries()) {
            String rowtext = "";
            // Print the first column's cell value
            rowtext += row.getTitle().getPlainText() + "\t";
            // Iterate over the remaining columns, and print each cell value
            for (String tag : row.getCustomElements().getTags()) {
                rowtext += row.getCustomElements().getValue(tag) + "\t";
            }

            Log.i("Row", rowtext);
        }
    }

    public List getSheetList() throws IOException,
            ServiceException {
        SpreadsheetFeed feed = service.getFeed(factory.getSpreadsheetsFeedUrl(),
                SpreadsheetFeed.class);
        List spreadsheets = feed.getEntries();
        return spreadsheets;
    }

    /**
     * Sets the particular cell at row, col to the specified formula or value.
     *
     * @param row the row number, starting with 1
     * @param col the column number, starting with 1
     * @param formulaOrValue the value if it doesn't start with an '=' sign; if it
     *        is a formula, be careful that cells are specified in R1C1 format
     *        instead of A1 format.
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    public void setCell(int row, int col, String formulaOrValue)
            throws IOException, ServiceException {

        CellEntry newEntry = new CellEntry(row, col, formulaOrValue);
        service.insert(cellFeedUrl, newEntry);
    }

    public void setCell(CellEntry cellEntry)
            throws IOException, ServiceException {
        service.insert(cellFeedUrl, cellEntry);
    }

    public void setRow(ListEntry row) throws IOException, ServiceException{
        row = service.insert(listFeedUrl, row);
    }

    /**
     * Prints out the specified cell.
     *
     * @param cell the cell to print
     */
    public void printCell(CellEntry cell) {
        String shortId = cell.getId().substring(cell.getId().lastIndexOf('/') + 1);
        out.println(" -- Cell(" + shortId + "/" + cell.getTitle().getPlainText()
                + ") formula(" + cell.getCell().getInputValue() + ") numeric("
                + cell.getCell().getNumericValue() + ") value("
                + cell.getCell().getValue() + ")");
    }

    /**
     * Shows all cells that are in the spreadsheet.
     *
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    public void showAllCells() throws IOException, ServiceException {
        CellFeed feed = service.getFeed(cellFeedUrl, CellFeed.class);

        for (CellEntry entry : feed.getEntries()) {
            printCell(entry);
        }
    }

    /**
     * Shows a particular range of cells, limited by minimum/maximum rows and
     * columns.
     *
     * @param minRow the minimum row, inclusive, 1-based
     * @param maxRow the maximum row, inclusive, 1-based
     * @param minCol the minimum column, inclusive, 1-based
     * @param maxCol the maximum column, inclusive, 1-based
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    public void showRange(int minRow, int maxRow, int minCol, int maxCol)
            throws IOException, ServiceException {
        CellQuery query = new CellQuery(cellFeedUrl);
        query.setMinimumRow(minRow);
        query.setMaximumRow(maxRow);
        query.setMinimumCol(minCol);
        query.setMaximumCol(maxCol);
        CellFeed feed = service.query(query, CellFeed.class);

        for (CellEntry entry : feed.getEntries()) {
            printCell(entry);
        }
    }

    /**
     * Performs a full-text search on cells.
     *
     * @param fullTextSearchString a full text search string, with space-separated
     *        keywords
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    public void search(String fullTextSearchString) throws IOException,
            ServiceException {
        CellQuery query = new CellQuery(cellFeedUrl);
        query.setFullTextQuery(fullTextSearchString);
        CellFeed feed = service.query(query, CellFeed.class);

        out.println("Results for [" + fullTextSearchString + "]");

        for (CellEntry entry : feed.getEntries()) {
            printCell(entry);
        }
    }

    /**
     * Writes (to stdout) a list of the entries in the batch request in a human
     * readable format.
     *
     * @param batchRequest the CellFeed containing entries to display.
     */
    private void printBatchRequest(CellFeed batchRequest) {
        System.out.println("Current operations in batch");
        for (CellEntry entry : batchRequest.getEntries()) {
            String msg = "\tID: " + BatchUtils.getBatchId(entry) + " - "
                    + BatchUtils.getBatchOperationType(entry) + " row: "
                    + entry.getCell().getRow() + " col: " + entry.getCell().getCol()
                    + " value: " + entry.getCell().getInputValue();
            System.out.println(msg);
        }
    }

    /**
     * Returns a CellEntry with batch id and operation type that will tell the
     * server to update the specified cell with the given value. The entry is
     * fetched from the server in order to get the current edit link (for
     * optimistic concurrency).
     *
     * @param row the row number of the cell to operate on
     * @param col the column number of the cell to operate on
     * @param value the value to set in case of an update the cell to operate on
     *
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    private CellEntry createUpdateOperation(int row, int col, String value)
            throws ServiceException, IOException {
        String batchId = "R" + row + "C" + col;
        URL entryUrl = new URL(cellFeedUrl.toString() + "/" + batchId);
        CellEntry entry = service.getEntry(entryUrl, CellEntry.class);
        entry.changeInputValueLocal(value);
        BatchUtils.setBatchId(entry, batchId);
        BatchUtils.setBatchOperationType(entry, BatchOperationType.UPDATE);

        return entry;
    }

    /**
     * Prompts the user for a set of operations and submits them in a batch
     * request.
     *
     * @param reader to read input from the keyboard.
     *
     * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
     * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
     */
    public void processBatchRequest(BufferedReader reader)
            throws IOException, ServiceException {

        final String BATCH_PROMPT = "Enter set operations one by one, "
                + "then enter submit to send the batch request:\n"
                + " set row# col# value  [[add a set operation]]\n"
                + " submit               [[submit the request]]";

        CellFeed batchRequest = new CellFeed();

        // Prompt user for operation
        System.out.println(BATCH_PROMPT);
        String operation = reader.readLine();
        while (!operation.startsWith("submit")) {
            String[] s = operation.split(" ", 4);
            if (s.length != 4 || !s[0].equals("set")) {
                System.out.println("Invalid command: " + operation);
                operation = reader.readLine();
                continue;
            }

            // Create a new cell entry and add it to the batch request.
            int row = Integer.parseInt(s[1]);
            int col = Integer.parseInt(s[2]);
            String value = s[3];
            CellEntry batchOperation = createUpdateOperation(row, col, value);
            batchRequest.getEntries().add(batchOperation);

            // Display the current entries in the batch request.
            printBatchRequest(batchRequest);

            // Prompt for another operation.
            System.out.println(BATCH_PROMPT);
            operation = reader.readLine();
        }

        // Get the batch feed URL and submit the batch request
        CellFeed feed = service.getFeed(cellFeedUrl, CellFeed.class);
        Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
        URL batchUrl = new URL(batchLink.getHref());
        CellFeed batchResponse = service.batch(batchUrl, batchRequest);

        // Print any errors that may have happened.
        boolean isSuccess = true;
        for (CellEntry entry : batchResponse.getEntries()) {
            String batchId = BatchUtils.getBatchId(entry);
            if (!BatchUtils.isSuccess(entry)) {
                isSuccess = false;
                BatchStatus status = BatchUtils.getBatchStatus(entry);
                System.out.println("\n" + batchId + " failed (" + status.getReason()
                        + ") " + status.getContent());
            }
        }
        if (isSuccess) {
            System.out.println("Batch operations successful.");
        }
    }

    /**
     * Runs the demo.
     *
     * @param args the command-line arguments
     * @throws AuthenticationException if the service is unable to validate the
     *         username and password.
     */

    /*
    public static void main(String[] args) throws AuthenticationException {
        SimpleCommandLineParser parser = new SimpleCommandLineParser(args);
        String username = parser.getValue("username", "user", "u");
        String password = parser.getValue("password", "pass", "p");
        boolean help = parser.containsKey("help", "h");

        if (help || username == null || password == null) {
            usage();
            System.exit(1);
        }

        CellDemo demo = new CellDemo(new SpreadsheetService("Cell Demo"),
                System.out);

        demo.run(username, password);
    }
    */

    /**
     * Prints out the usage.
     */
    private static void usage() {
        for (String s : USAGE_MESSAGE) {
            System.out.println(s);
        }
        for (String s : WELCOME_MESSAGE) {
            System.out.println(s);
        }
    }
}
