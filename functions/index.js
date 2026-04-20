/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const {GoogleGenerativeAI} = require("@google/generative-ai");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

exports.parseNotification = onCall(
    {
        secrets: ["GEMINI_API_KEY"]
    },
    async (request) => {
    const {packageName, postTime, text, bigText} = request.data;

    const model = genAI.getGenerativeModel({
        model: "gemini-2.5-flash",
        generationConfig: {
            responseMimeType: "application/json"
        }
    });

    // 合并后的单次 Prompt
    const prompt = `
    You are a transaction detector for SmartLedger AI, a personal finance tracking app.
    Your ONLY job is to determine if a mobile notification is about a financial transaction,
    and if yes, extract basic fields. You do NOT categorize — the user will do that manually.

    Rules:
    - Only detect: payments, transfers, top-ups, refunds, salary/income received
    - Ignore: promotions, ads, OTP codes, login alerts, delivery status, chat messages
    - If unsure, set isTransaction to false
    - merchant should be the human-readable business name, not the app package name
    - type is from the USER's perspective: money out = SPENDING, money in = INCOME, between own accounts = TRANSFER

    Return ONLY a valid JSON object:
    {
      "isTransaction": boolean,
      "amount": number | null,
      "currency": "MYR" | "USD" | "SGD" | null,
      "merchant": string | null,
      "type": "SPENDING" | "INCOME" | "TRANSFER" | null,
      "reason": string
    }

    Notification source app: ${packageName}
    Text: ${text}
    Extended: ${bigText}
    `;

    try {
        const result = await model.generateContent(prompt);
        // 因为设置了 responseMimeType，直接 parse 即可，无需正则清理
        const responseData = JSON.parse(result.response.text());

        // 如果不是交易，直接返回
        if (!responseData.isTransaction) {
            return {
                isTransaction: false,
                packageName,
                postTime,
                reason: responseData.reason
            };
        }

        // 如果是交易，返回完整数据
        return {
            isTransaction: true,
                amount: responseData.amount,
                currency: responseData.currency || "MYR",
                merchant: responseData.merchant || null,
                type: responseData.type || "SPENDING",
                packageName,
                postTime
        };

    } catch (error) {
        console.error("Gemini API Error:", error);
        console.error("Full error:", JSON.stringify(error));
        throw new HttpsError("internal", error.message);
    }
});
