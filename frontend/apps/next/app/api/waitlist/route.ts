import { NextRequest, NextResponse } from "next/server";
import { google } from "googleapis";

export async function POST(req: NextRequest) {
  try {
    const { email, profession } = await req.json();

    // 1. Parse the Secret Key from Vercel Environment Variables
    const credentials = JSON.parse(process.env.GOOGLE_SERVICE_ACCOUNT_JSON!);

    // 2. Initialize Google Auth
    const auth = new google.auth.GoogleAuth({
      credentials,
      scopes: ["https://www.googleapis.com/auth/spreadsheets"],
    });

    const sheets = google.sheets({ version: "v4", auth });

    // 3. Append the data to your Google Sheet
    // Make sure you have GOOGLE_SHEET_ID in your Vercel Env Variables!
    await sheets.spreadsheets.values.append({
      spreadsheetId: process.env.GOOGLE_SHEET_ID,
      range: "Sheet1!A:B", // Adjust this if your sheet tab is named differently
      valueInputOption: "USER_ENTERED",
      requestBody: {
        values: [[email, profession, new Date().toISOString()]],
      },
    });

    return NextResponse.json({ message: "Success" }, { status: 200 });
  } catch (error: any) {
    console.error("Google Sheets Error:", error);
    return NextResponse.json(
      { error: "Failed to join waitlist" },
      { status: 500 }
    );
  }
}