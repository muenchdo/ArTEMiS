import { Injectable } from '@angular/core';
import { SERVER_API_URL } from 'app/app.constants';
import { HttpClient, HttpParameterCodec, HttpParams } from '@angular/common/http';
import { Cacheable } from 'ngx-cacheable';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProgrammingExercisePlantUmlService {
    private resourceUrl = SERVER_API_URL + 'api/plantuml';
    private encoder: HttpParameterCodec;

    /**
     * Cacheable configuration
     */

    constructor(private http: HttpClient) {
        this.encoder = new HttpUrlCustomEncoder();
    }

    /**
     * Requests the plantuml png file as arraybuffer and converts it to base64.
     * @param plantUml - definition obtained by parsing the README markdown file.
     *
     * TODO provide a rationale about the cache configuration
     *
     */
    @Cacheable({
        /** Cacheable configuration **/
        maxCacheCount: 3,
        maxAge: 3000,
        slidingExpiration: true,
    })
    getPlantUmlImage(plantUml: string) {
        return this.http
            .get(`${this.resourceUrl}/png`, {
                params: new HttpParams({ encoder: this.encoder }).set('plantuml', plantUml),
                responseType: 'arraybuffer',
            })
            .map((res) => this.convertPlantUmlResponseToBase64(res));
    }

    /**
     * Requests the plantuml svg as string.
     * @param plantUml - definition obtained by parsing the README markdown file.
     *
     * TODO provide a rationale about the cache configuration
     *
     */
    @Cacheable({
        /** Cacheable configuration **/
        maxCacheCount: 3,
        maxAge: 3000,
        slidingExpiration: true,
    })
    getPlantUmlSvg(plantUml: string): Observable<string> {
        return this.http.get(`${this.resourceUrl}/svg`, {
            params: new HttpParams({ encoder: this.encoder }).set('plantuml', plantUml),
            responseType: 'text',
        });
    }

    private convertPlantUmlResponseToBase64(res: any): string {
        return Buffer.from(res, 'binary').toString('base64');
    }
}

/**
 * @class HttpUrlCustomEncoder
 * @desc Custom HttpParamEncoder implementation which defaults to using encodeURIComponent to encode params
 */
export class HttpUrlCustomEncoder implements HttpParameterCodec {
    /**
     * Encodes key.
     * @param k - key to be encoded.
     */
    encodeKey(k: string): string {
        return encodeURIComponent(k);
    }

    /**
     * Encodes value.
     * @param v - value to be encoded.
     */
    encodeValue(v: string): string {
        return encodeURIComponent(v);
    }

    /**
     * Decodes key.
     * @param k - key to be decoded.
     */
    decodeKey(k: string): string {
        return decodeURIComponent(k);
    }

    /**
     * Decodes value.
     * @param v - value to be decoded.
     */
    decodeValue(v: string) {
        return decodeURIComponent(v);
    }
}
