import { AfterContentChecked, Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { EventType } from 'projects/tools/src/lib/classes/events';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { AuthenticationService } from '../../services/authentication.service';
import { Tools } from 'projects/tools/src/lib/tools.service';

@Component({
  selector: 'app-application',
  templateUrl: 'application.component.html',
  styleUrls: ['application.component.scss']
})
export class ApplicationComponent implements OnInit, AfterContentChecked {
  static readonly Name = 'ApplicationComponent';

  config: any;
  appConfig: any;

  _spin: boolean = false;
  desktop: boolean = false;

  gridCols = 3;

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Application', url: '', type: 'title', icon: 'dashboard' }
  ];

  _iframeUrl: string = '';
  _iframeUrlError: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private translate: TranslateService,
    private configService: ConfigService,
    public eventsManagerService: EventsManagerService,
    public pageloaderService: PageloaderService,
    public authenticationService: AuthenticationService,
    private sanitized: DomSanitizer
  ) {
    this.appConfig = this.configService.getConfiguration();

    this.configService.getConfig('application').subscribe(
      (config: any) => {
        this.config = config;

        if (Tools.CurrentApplication && Tools.CurrentApplication.menu && Tools.CurrentApplication.menu.url) {
          this._iframeUrl = Tools.CurrentApplication.menu.url;
        } else {
          this.router.navigate(['/dashboard']);
        }
      }
    );
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      // language changed
    });

    this.pageloaderService.resetLoader();
    this.pageloaderService.isLoading.subscribe({
      next: (x) => { this._spin = x; },
      error: (e: any) => { console.log('loader error', e); }
    });
  }

  ngOnDestroy() {
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
    if (Tools.CurrentApplication && Tools.CurrentApplication.menu && Tools.CurrentApplication.menu.url) {
      this._iframeUrl = Tools.CurrentApplication.menu.url;
    }
  }

  onLoadIFrame(event: any) {
    // console.log('iFrame Load', event);
  }

  _sanitizeUrl(url: string): SafeUrl {
    return this.sanitized.bypassSecurityTrustResourceUrl(url)
  }
}
