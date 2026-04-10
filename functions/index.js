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
    Analyze the following notification.
    1. Determine if it is a financial transaction notification.
    2. If it IS a transaction, extract the transaction amount and currency.
    3. If it is NOT a transaction, set amount and currency to null, and reply a reason.

    Return ONLY a valid JSON object strictly matching this structure:
    {
        "isTransaction": boolean,
        "amount": number | null,
        "curren"
        "currency": string | null ,
        "reason":string
    }

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
            currency: responseData.currency || "MYR", // 容错处理：如果没有抓取到货币，可设置默认值
            packageName,
            postTime
        };

    } catch (error) {
        console.error("Gemini API Error:", error);
        console.error("Full error:", JSON.stringify(error));
        throw new HttpsError("internal", error.message);
    }
});
